%% Function: nameOfFunction.m
%
% This script manages a GUI for the following tasks: selecting/opening a
% MAP file, starting and stopping processing/streaming, sliders to adjust
% microphone sensitivity, gain, and volume.
%           CCi-MOBILE Version: 2.2c
%           Created by: Hussnain Ali, 2016
%           Copyright Cochlear Ltd - derived from Nucleus MATLAB Toolbox v2*
%
% See 'README.txt' for more information
%% Beginning of function

clc;
clear all;
close all;
global fs;
fs = 16000;
t = 5000;       % time duration in ms. The script will run for 't'ms
check = 1;      % Check = 0 for any human testing

% Check = 1 for testing integrity of the output on oscilloscope
electrodogram_plot = 1; % = 1 will plot the electrodogram.

% If t is large, more memory will be required to store the variables,
% which could impact the performance and realtime capabilities.
% For large time durations, set electrodogram_plot = 0;

% add common functions path to the current directory
currentFolder = pwd;
CCIMobileFolder = fileparts(currentFolder); % currentFolder(1:end-8);
CommonFunctionsFolder = [CCIMobileFolder '\CommonFunctions\'];
addpath(CommonFunctionsFolder);

% Initialize
p = initialize_ACE_integer_ppf;
s = initializeBoard(p);
outputBuffer = create_output_buffer(p);

cl=[];el=[];
p.General.LeftOn = 0;
p.General.RightOn = 0;
if (isfield(p,'Left') ==1)
    sine_token_l=uint8(abs(150.*sin(2.*pi.*(0:1:p.Left.pulses_per_frame).*0.5/p.Left.pulses_per_frame))); %figure; plot(sine_token);
    %sine_token_l=uint8(150.*sin(2.*pi.*(0:1:p.Left.pulses_per_frame).*0.01)); %figure; plot(sine_token);
    indL = 1;
    bufferHistory_left = (zeros(1, p.Left.block_size - p.Left.block_shift));
    p.General.LeftOn = 1;
end

if (isfield(p,'Right') ==1)
    sine_token_r=uint8(abs(150.*sin(2.*pi.*(0:1:p.Right.pulses_per_frame).*0.5/p.Right.pulses_per_frame))); %figure; plot(sine_token);
    %sine_token_r=uint8(150.*sin(2.*pi.*(0:1:p.Right.pulses_per_frame).*0.01)); %figure; plot(sine_token);
    indR = 1;
    bufferHistory_right = (zeros(1, p.Right.block_size - p.Right.block_shift));
    p.General.RightOn = 1;
end

nframes = round(t/8);  % each frame is 8ms
frame_no=1;
while frame_no<nframes % use while else timing won't be right
    if (Wait(s)>= 512)
        AD_data_bytes = Read(s, 512);                       % Read audio from BTE
        AD_data=typecast(int8(AD_data_bytes), 'int16');     % Type cast to short 16 bits
        %tic;
        if (p.General.LeftOn == 1)
            audio_left = double(AD_data(1:2:end));          % Type cast to double for processing
            audio_left =  (p.Left.scale_factor).*audio_left; %2.3/32768 at 25 dB gain or 1/32768 at 33dB gain for 1kHz at 65dB SPl to equate to MCL p.sensitivity.
            stimuli.left = ACE_Processing_Realtime(audio_left, bufferHistory_left, p.Left);
            
            pulses_per_frame_left = numel(stimuli.left.electrodes);
            a=7;
            for i=1:pulses_per_frame_left
                outputBuffer(a) = uint8(stimuli.left.electrodes(i));
                a=a+1; %left electrodes
            end
            
            a= 133;
            for i=1:pulses_per_frame_left
                outputBuffer(a) = uint8(stimuli.left.current_levels(i));
                a=a+1; %left amplitudes
            end
            bufferHistory_left = audio_left(p.Left.block_size-p.Left.NHIST+1:end);
        else
            %a= 133;
            outputBuffer(133:248) = uint8(0); %zero out left amplitudes, if not active
        end
        
        if (p.General.RightOn == 1)
            audio_right=double(AD_data(2:2:end));
            audio_right = (p.Right.scale_factor).*audio_right;
            stimuli.right = ACE_Processing_Realtime(audio_right, bufferHistory_right, p.Right);
            pulses_per_frame_right = numel(stimuli.right.electrodes);
            
            a=265;
            for i=1:numel(stimuli.right.electrodes)
                outputBuffer(a) =uint8(stimuli.right.electrodes(i));
                a=a+1; %right electrodes
            end
            a=391;
            for i=1:numel(stimuli.right.electrodes)
                outputBuffer(a) = uint8(stimuli.right.current_levels(i));
                a=a+1; %right amplitudes
            end
            bufferHistory_right = audio_right(p.Right.block_size-p.Right.NHIST+1:end);
        else
            %a=391;
            outputBuffer(391:506) = uint8(0); %zero out right amplitudes
        end
        
        % t2=toc;
        % time(frame_no)=t2; % Time profile to process each frame
        
        if check==0 % Write the BTE processed stimuli to the coil
            Write(s, outputBuffer,516);
        else
            % To check the integrity of the output stimuli on oscilloscope
            % If check = 1, you would see sine wave on electrodes 1 - 8
            % A regular sine wave indicates that timing is correct
            for j=1:p.Left.pulses_per_frame/p.Left.Nmaxima %p.left.Nmaxima
                stim.l(j)= 200;%sine_token_l(indL);
                indL=indL+1;
                if indL==length(sine_token_l)+1
                    indL=1;
                end
            end
            
            for j=1:p.Right.pulses_per_frame/p.Right.Nmaxima %right.Nmaxima
                stim.r(j)= sine_token_r(indR);
                indR=indR+1;
                if indR==length(sine_token_r)+1
                    indR=1;
                end
            end
            stimulus = UART_output_buffer(stim, p);
            Write(s, stimulus,516);
        end
        
        frame_no = frame_no+1;
        if (electrodogram_plot == 1)
            cl = [cl; stimuli.left.current_levels];
            el = [el; stimuli.left.electrodes];
        end
    end
end % end while loop for nframes

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

delete(s);
clear s;
% figure; plot(time(10:end));
% average_time = sum(time)/nframes

%% ELECTRODGRAM PLOT - Specify parameters for plotting electrodogram
if (electrodogram_plot == 1)
    q.current_levels = cl;
    q.electrodes = el;
    q.phase_widths = p.Left.PulseWidth;
    q.phase_gaps = p.Left.IPG;
    q.modes = 108;
    q.periods = 1e6/(p.Left.StimulationRate*p.Left.Nmaxima);
    plot_electrodogram(q,'Electrodogram');
end