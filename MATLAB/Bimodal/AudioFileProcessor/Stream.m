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
%% Stream
indL = 0; indR = 0;
aindL = 1; aindR = 1;
frame_no=1;
%la = scale_factor_l.*stimuli.left.audio;
%figure; subplot(3,1,1); plot(stimuli.left.audio); subplot(3,1,2); plot(la);subplot(3,1,3); plot(int16(la));
%typecast(int16(scale_factor_l.*stimuli.left.audio(aindL:aindL+127)),'uint8');
%la = buffer(stimuli.left.audio,128);
audio_index = 1;
interleaved_audio = zeros(1,2*numel(stimuli.left.audio));
for i=1:numel(stimuli.left.audio)
    interleaved_audio(audio_index) = stimuli.left.audio(i);
    interleaved_audio(audio_index+1) = stimuli.right.audio(i);
    audio_index = audio_index + 2;
end

interleaved_audio_bytes = typecast(int16(interleaved_audio),'uint8');
interleaved_audio_bytes_frames = buffer(interleaved_audio_bytes,128*4);

while frame_no<nframes-1 % use while else timing won't be right
    if Wait(s)>= 512
        AD_data_bytes = Read(s, 512);
        
        if (isfield(p,'Left') ==1)&&(p.General.LeftOn == 1)
            if frame_no<=nframesL
                a=7;
                for (i=1:npulsesL)
                    outputBuffer(a) = uint8(stimuli.left.electrodes(i+indL)); a=a+1; %left electrodes
                end
                a= 133;
                for (i=1:npulsesL)
                    outputBuffer(a) = uint8(stimuli.left.current_levels(i+indL)); a=a+1; %left amplitudes
                end
                %                 a=518;b=1;
                %                 for (i=1:128)
                %                     outputBuffer(a:a+1) = interleaved_audio_bytes_frames(frame_no,b:b+1); a=a+4;b=b+2; %left audio
                %                 end
                indL = indL + npulsesL;
                
            else
                %a= 133;
                outputBuffer(133:248) = uint8(0); %zero out left amplitudes, if not active
            end
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
                %right_audio_bytes = typecast(int16(stimuli.right.audio(aindR:aindR+127)),'uint8'); aindR = aindR +128;
                %                 a=520; b = 1;
                %                 for (i=1:128)
                %                     outputBuffer(a:a+1) = right_audio_bytes(b:b+1); a=a+4; b=b+2; %left audio
                %                 end
                indR = indR + npulsesR;
                
            else
                %a = 391;
                outputBuffer(391:506) = uint8(0); %zero out right amplitudes
            end
        else
            %a = 391;
            outputBuffer(391:506) = uint8(0); %zero out right amplitudes
        end
        
        %% audio write
        %a = 518;
        outputBuffer(518:1029) = interleaved_audio_bytes_frames(:,frame_no);
        
        Write(s, outputBuffer,numel(outputBuffer)); % Write output to the board
        
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
        outputBuffer(518:1029) = uint8(0); %zero out audio bytes
        Write(s, outputBuffer,numel(outputBuffer)); % Write output to the board
        frame_no = frame_no+1;
    end
    
end
delete(s); clear s;

