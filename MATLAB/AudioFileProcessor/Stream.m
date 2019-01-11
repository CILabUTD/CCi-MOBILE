function Stream(stimuli)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Funtion Stream(signal) reads the stimuli and its parameters from the input
% structure signal and streams them to the CCIMobile platform

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%             Author: Hussnain Ali
%               Date: 06/21/16
% University of Texas at Dallas (c) Copyright 2016
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
p=stimuli.map;
s = initializeBoard(p);
outputBuffer = create_output_buffer(p);

if (isfield(p,'Left') ==1)
    npulsesL = p.Left.pulses_per_frame;
    total_pulses_L = numel(stimuli.left.current_levels);
    npulses_per_second = p.Left.StimulationRate*p.Left.Nmaxima;
    durationL = total_pulses_L/npulses_per_second;
    nframesL = durationL/8e-3; %for each 8ms frame
else
    npulsesL = 0; total_pulses_L = 0; nframesL = 0;
end
if (isfield(p,'Right') ==1)
    npulsesR = p.Right.pulses_per_frame;
    total_pulses_R = numel(stimuli.right.current_levels);
    npulses_per_second = p.Right.StimulationRate*p.Right.Nmaxima;
    durationR = total_pulses_R/npulses_per_second;
    nframesR = durationR/8e-3; %for each 8ms frame
else
    npulsesR = 0; total_pulses_R = 0;  nframesR=0;
end

nframes = max(nframesL,nframesR); % while loop will run for nframes

if (isfield(p,'Left') ==1)
    %if pulses cannot be evenly distributed in all frames
    pulses_per_frame = total_pulses_L/nframesL;
    pulses_per_frame = (round(10*pulses_per_frame))/10; % round to 1 decimal place
    npulsesL(1) = ceil(pulses_per_frame);
    fract = npulsesL(1) - pulses_per_frame;
    for i=1:nframes-1
        difference = pulses_per_frame-fract;
        npulsesL(i+1)=ceil(difference);
        fract = (npulsesL(i+1)-difference);
    end
    interpulseDuration = 8000./(pulses_per_frame) - 2*p.Left.PulseWidth - p.Left.IPG - p.General.durationSYNC - p.General.additionalGap;
    ncyclesL = uint16((interpulseDuration./0.1));nRFcycles= typecast(ncyclesL,'uint8');
    outputBuffer(511)=nRFcycles(2); outputBuffer(512)=nRFcycles(1);
end

if (isfield(p,'Right') ==1)
    %if pulses cannot be evenly distributed in all frames
    pulses_per_frame = total_pulses_R/nframesR;
    pulses_per_frame = (round(10*pulses_per_frame))/10;  % round to 1 decimal place
    npulsesR(1) = ceil(pulses_per_frame);
    fractR = npulsesR(1)-pulses_per_frame;
    for i=1:nframes-1
        difference = pulses_per_frame-fractR;
        npulsesR(i+1)=ceil(difference);
        fractR = (npulsesR(i+1)-difference);
    end
    interpulseDuration = 8000./(pulses_per_frame) - 2*p.Right.PulseWidth - p.Right.IPG - p.General.durationSYNC - p.General.additionalGap;
    ncyclesR = uint16((interpulseDuration./0.1)); nRFcycles= typecast(ncyclesR,'uint8');
    outputBuffer(513)=nRFcycles(2); outputBuffer(514)=nRFcycles(1);
end

%% Stream
indL = 0; indR = 0;
frame_no=1;

while frame_no<nframes % use while else timing won't be right
    
    if Wait(s)>= 512
        AD_data_bytes = Read(s, 512);
        
        if (isfield(p,'Left') ==1)&&(p.General.LeftOn == 1)
            if frame_no<=nframesL
                a=7;
                for (i=1:npulsesL(frame_no))
                    outputBuffer(a) = uint8(stimuli.left.electrodes(i+indL)); a=a+1; %left electrodes
                end
                a= 133;
                for (i=1:npulsesL(frame_no))
                    outputBuffer(a) = uint8(stimuli.left.current_levels(i+indL)); a=a+1; %left amplitudes
                end
                indL = indL + npulsesL(frame_no);
            else
                %a= 133;
                outputBuffer(133:248) = uint8(0); %zero out left amplitudes, if not active
            end
            ppfL = uint16(npulsesL(frame_no)); pulsesPerFrameL = typecast(ppfL,'uint8');
            outputBuffer(507)=pulsesPerFrameL(2); %left pulsesPerFrame high[15:8]
            outputBuffer(508)=pulsesPerFrameL(1); %left pulsesPerFrame low[7:0]
        else
            %a= 133;
            outputBuffer(133:248) = uint8(0); %zero out left amplitudes, if not active
        end
        
        if (isfield(p,'Right') ==1)&&(p.General.RightOn == 1)
            if frame_no<=nframesR
                a=265;
                for (i=1:npulsesR)
                    outputBuffer(a) =uint8(stimuli.right.electrodes(i+indR)); a=a+1; %right electrodes
                end
                a=391;
                for (i=1:npulsesR)
                    outputBuffer(a) = uint8(stimuli.right.current_levels(i+indR)); a=a+1; %right amplitudes
                end
                indR = indR + npulsesR(frame_no);
            else
                %a = 391;
                outputBuffer(391:506) = uint8(0); %zero out right amplitudes
            end
            ppfR = uint16(npulsesR(frame_no)); pulsesPerFrameR = typecast(ppfR,'uint8');
            outputBuffer(509)=pulsesPerFrameR(2); %right pulsesPerFrame high[15:8]
            outputBuffer(510)=pulsesPerFrameR(1); %right pulsesPerFrame low[7:0]
        else
            %a = 391;
            outputBuffer(391:506) = uint8(0); %zero out right amplitudes
        end
        
        Write(s, outputBuffer,516); % Write output to the board
        frame_no = frame_no+1;
    end
    
end

%clear stimulation buffers by sending null frames
frame_no=1;
while frame_no<3 % send at least 2 null frames to clear out memory
    if Wait(s)>= 512
        AD_data_bytes = Read(s, 512);
        outputBuffer(133:248) = uint8(0); %zero out left amplitudes, if not active
        outputBuffer(391:506) = uint8(0); %zero out right amplitudes
        Write(s, outputBuffer,516); % Write output to the board
        frame_no = frame_no+1;
    end
    
end
delete(s); clear s;
