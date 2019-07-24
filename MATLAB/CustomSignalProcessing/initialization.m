function [p,app] = initialization(realtime,priority,manual,app)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Initialization of ACE processing from subject's MAP and user-specific
% signal processing parameters. This function is called by running,
% 'AudioFileProcessor.m'
%                                    Created: Juliana N. Saba (Jan, 2019)
% INPUTS:
%   realtime    = (0 - off, 1 - on) If on, all integers and rounded values
%       result in an integer byte transfer size to ensure no loss in data 
%       or samples. If off, data may be lost due to rounding errors.
%   priority    = (0 - off, 1 - on) If on, it will call the function:
%       'userSpecificPriorityParameters.m' which lets the user determine 
%       the heirarchy of parameters for exact user-specification of signal
%       processing parameters (i.e. priority). 
%   manual      = (0 - off, 1 - on) If on, a MAP file must be selected
%       using the function funcCVSWL_MAP.m() where manual is the map_select
%       number to choose from. Load all MAP configurations into the above
%       function. 

% OUTPUT:
%   p = A structure that loads all of the MAP parameters in 'p.General',
%       'p.Left' and 'p.Right'; this structure is passed through the
%       function, 'AudioFileProcessor.m'

% EMBEDDED FUNCTIONS (in order of appearance):
% 'field_check.m' = Loads the default parameters of the 'SampleMap.m' and
%                   checks if any of the fields are missing. If there is 
%                   missing data, the program will notify the user and set 
%                   the missing value to it's default value (see the 
%                   comments in 'SampleMap.m' for default values).
% 'timing_check.m'  = This function checks the pulse width to ensure the 
%                     user-specific parameters meet operating specifications
%                     based on the ver 2.2c algorithm:
%                     'check_timing_parameters.m'
% 'level_check.m' = This function checks each channel's clinical level to
%                   ensure only valid values of 1-255 are input from the
%                   user-specified MAP. 
% 'checkOperatingLimits.m' = See function description in program for more
%                     details. This function gives the user the 
%                     option to display notifications when
%                     adjustments are made based on operating
%                     specifications.
% 'userSpecificPriorityParameters.m' = See function description in program
%                   or more information. This program can be used to 
%                   specify the user-specific parameters to control the
%                   adjustment order and heirarchy of parameters. 

%% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Begin initialization
global fs
fs = 16000;
verbose = 0;
if manual == 0
    % Load MAP Parameters - Previously used program: 'load_map.m' (ver 2.2c)
    [map_filename,map_pathname] = uigetfile('*.m', 'Select a patient map file');
    if isequal(map_filename,0)
        disp('Please load a patient map file before proceeding')
        close all;
        return
    else
        disp(['Map file loaded is: ', fullfile(map_pathname, map_filename)])
    end
    % Read subject MAP file
    map_address = [map_pathname map_filename];
    % Load the MAP file
    run(map_address);
end

% Check MAP for errors: rate, pulse width, THR, MCL values
% Step 1. Check individual fields - Previously used program: 'check_map.m' (ver 2.2c)
stepone   = field_check(MAP);
% Step 2. Check timing - Previously used program: 'check_timing.m' (ver 2.2c)
steptwo   = timing_check(stepone);
% Step 3. Check clinical levels - Previously used program: 'level_check.m' (ver 2.2c)
stepthree = level_check(steptwo);
% Final checked MAP is loaded as 'p'
p         = stepthree; 
% Clear steps
clear stepone steptwo stepthree

% Begin initialization steps
p.General.block_size        = 128;
p.General.frameDuration     = 8;
p.General.durationSYNC      = 6;
p.General.additionalGap     = 1;
p.General.fft_size          = 128;
p.General.num_bins          = p.General.block_size/2 + 1;
p.General.bin_freq          = fs/p.General.block_size;
p.General.bin_freqs         = p.General.bin_freq*(0:p.General.num_bins-1);
p.General.sub_mag           = -1e-10;

% Process left and right ear/MAP independently:
% ----------------------- LEFT MAP (if exists) ------------------------ %
if (isfield(p,'Left')==1)
 if (priority == 0)
  if (realtime == 0)
    % Offline processing can buffer non-integer values for pulses per frame
    p.Left.block_size       = p.General.block_size;
    p.Left.nvects           = p.Left.pulses_per_frame_per_channel;
    p.Left.analysis_rate    = p.Left.StimulationRate;
    p.Left.block_shift      = ceil(fs/p.Left.analysis_rate);
    p.Left.analysis_rate    = round(fs/p.Left.block_shift);
    p.Left.StimulationRate  = p.Left.analysis_rate;
    p.Left.total_rate       = p.Left.analysis_rate*p.Left.Nmaxima;
    p.Left.pulses_per_frame_per_channel = ((8.0*p.Left.analysis_rate)/1000); 
    p.Left.pulses_per_frame = floor(p.Left.Nmaxima*p.Left.pulses_per_frame_per_channel);
    % Offline processing does not ensure IPD < 6 us
    p.Left.interpulseDuration = (1e6/p.Left.total_rate) - 2*p.Left.PulseWidth - p.Left.IPG - p.General.durationSYNC - p.General.additionalGap;
        % CHECK OPERATING LIMITS
        [app] = checkOperatingLimits(app,MAP.Left,p.Left,1,verbose,0,'L');
        % p.Left.flag = flag;
  else
    % Realtime processing ensures INTEGER VALUES for pulses per frame
    p.Left.block_size       = p.General.block_size;
    p.Left.nvects           = p.Left.pulses_per_frame_per_channel;
    p.Left.analysis_rate    = p.Left.StimulationRate;
    p.Left.block_shift      = ceil(fs/p.Left.analysis_rate);
    p.Left.analysis_rate    = fs/p.Left.block_shift;
    p.Left.pulses_per_frame_per_channel = floor(8.0/1000*p.Left.analysis_rate); 
    % ORIGINAL: p.Left.pulses_per_frame_per_channel = floor((8.0*p.Left.analysis_rate)/1000); 
    p.Left.pulses_per_frame = (p.Left.Nmaxima*p.Left.pulses_per_frame_per_channel);
    p.Left.StimulationRate  = (p.Left.pulses_per_frame*1000)/(8*p.Left.Nmaxima);
    p.Left.total_rate       =  p.Left.analysis_rate*p.Left.Nmaxima;  
    % Realtime processing ensures IPD < 6 us
    p.Left.interpulseDuration = p.General.frameDuration*1000/(p.Left.pulses_per_frame) - 2*p.Left.PulseWidth - p.Left.IPG - p.General.durationSYNC - p.General.additionalGap;
        % CHECK OPERATING LIMITS
        [app] = checkOperatingLimits(app,MAP.Left,p.Left,1,verbose,0,'L');
        % p.Left.flag = flag;
  end % end realtime - on/off
 else
    % Priority is on
    % Priority parameters to be released in Ver 2.2e
    % [p.Left] = userSpecificPriorityParameters(p.Left);
 end % end priority - on/off
 
    % Define pulses
    pwL                     = uint16(p.Left.PulseWidth); 
    p.Left.pulseWidth       = typecast(pwL,'uint8');
    ppfL                    = uint16(p.Left.pulses_per_frame); 
    p.Left.pulsesPerFrame   = typecast(ppfL,'uint8');
    p.Left.ncycles          = uint16((p.Left.interpulseDuration/0.1));
    p.Left.nRFcycles        = typecast(p.Left.ncycles,'uint8');
    % Check Implant Type
    switch (p.Left.ImplantType)
        case 'CI24RE'
            p.Left.ImplantGeneration = 'CIC4';
        case 'CI24R'
            p.Left.ImplantGeneration = 'CIC4';
        case 'CI24M'
            p.Left.ImplantGeneration = 'CIC3';
        otherwise
            disp('Left Implant: Check implant chip type');
            p.Left.ImplantGeneration = 'CIC4';
    end
    % Implant Generation
    if strcmp(p.Left.ImplantGeneration, 'CIC4')
        switch (p.Left.StimulationMode)
            case 'MP1+2'
                p.Left.StimulationModeCode = 28; 
            otherwise
                disp('Update stimulation mode code for the stimulation mode');
                p.Left.StimulationModeCode = 28;
        end
    end
    % Implant Type
    if strcmp(p.Left.ImplantGeneration, 'CIC3')
        switch (p.Left.StimulationMode)
            case 'MP1+2'
                p.Left.StimulationModeCode = 30; %only for CIC3 chip type
            otherwise
                disp('Update stimulation mode code for the stimulation mode');
                p.Left.StimulationModeCode = 28; %MP1+2
        end
    end
    % Stimulation Order
    p.Left.sub_mag = p.General.sub_mag;
    switch (p.Left.ChannelOrderType)
        case 'base-to-apex'
            p.Left.channel_order = (p.Left.NumberOfBands:-1:1)';
        case 'apex-to-base'
            p.Left.channel_order = (1:1:p.Left.NumberOfBands)';
    end
    % Frequency-Time Matric Parameters
    p.Left.ranges       = p.Left.MCL - p.Left.THR;
    p.Left.global_gain  = 1;
    p.Left.NHIST        = p.Left.block_size-p.Left.block_shift;
    p.Left.fft_size     = p.General.fft_size;
    p.Left.num_bins     = p.General.num_bins;
    p.Left.bin_freq     = p.General.bin_freq;
    p.Left.bin_freqs    = p.General.bin_freqs;
    % Window
    switch (p.Left.Window)
        case 'Hanning'
            a = [0.5, 0.5, 0.0, 0.0 ];
        case 'Hamming'
            a = [0.54, 0.46, 0.0, 0.0 ];
        case 'Blackman'
            a = [0.42, 0.5, 0.08, 0.0 ];
        otherwise
            a = [0.5, 0.5, 0.0, 0.0 ];
    end
    n = (0:p.Left.block_size-1)';		
    r = 2*pi*n/p.Left.block_size;		
    p.Left.window = a(1) - a(2)*cos(r) + a(3)*cos(2*r) - a(4)*cos(3*r);
    % Filter Weights
    p.Left.band_bins = FFT_band_bins(p.Left.NumberOfBands)';
    % Weights matrix for combining FFT bins into bands: LEFT
    p.Left.weights = zeros(p.Left.NumberOfBands, p.Left.num_bins);
    bin = 3;	% We always ignore bins 0 (DC) & 1.
    for band = 1:p.Left.NumberOfBands
        width = p.Left.band_bins(band);
        p.Left.weights(band, bin:(bin + width - 1)) = 1;
        bin = bin + width;
    end
        % Optionally incorporate frequency response equalisation:
        freq_response  = freqz(p.Left.window/2, 1, p.Left.block_size);
        power_response = freq_response .* conj(freq_response);
        P1 = power_response(1);
        P2 = 2 * power_response(2);
        P3 = power_response(1) + 2 * power_response(3);
    % Power Gains
    p.Left.power_gains = zeros(p.Left.NumberOfBands, 1);
    for band = 1:p.Left.NumberOfBands
        width = p.Left.band_bins(band);
        if (width == 1)
            p.Left.power_gains(band) = P1;
        elseif (width == 2)
            p.Left.power_gains(band) = P2;
        else
            p.Left.power_gains(band) = P3;
        end
    end
    % Apply Gains to Bands
    for band = 1:p.Left.NumberOfBands
        p.Left.weights(band, :) = p.Left.weights(band, :) / p.Left.power_gains(band);
    end
    % Frequency boundaries of triangular filters
    cum_num_bins            = [1.5; 1.5 + cumsum(p.Left.band_bins)];
    p.Left.crossover_freqs  = cum_num_bins * p.Left.bin_freq;
    p.Left.band_widths      = diff(p.Left.crossover_freqs);
    p.Left.char_freqs       = p.Left.crossover_freqs(1:p.Left.NumberOfBands) + p.Left.band_widths/2;
    % Sensitivity
    p.Left.sensitivity  = 2.3; p.Left.scale_factor = p.Left.sensitivity/32768;
    % Gains
    p.Left.gains_dB     = p.Left.Gain+ p.Left.BandGains;
    p.Left.gains        = 10 .^ (p.Left.gains_dB / 20.0);  %p.Left.gains(23-p.Left.off_electrodes)=[];
    % Volume
    p.Left.volume_level = p.Left.Volume/10;
    % Channel Selection
    p.Left.num_rejected = p.Left.NumberOfBands - p.Left.Nmaxima;
    
    % ORIGINAL: LGF Compression
    if (p.Left.BaseLevel > 0)
        % For information, not used in processing
        p.Left.lgf_dynamic_range = 20*log10(p.Left.SaturationLevel/p.Left.BaseLevel);
    end
    
    % --------------------------- LGF_alpha -------------------------- %
    % p.Left.lgf_alpha = LGF_alpha(p.Left.Q,p.Left.BaseLevel,p.Left.SaturationLevel);
    % alpha = LGF_alpha(Q,BaseLevel,SaturationLevel)
    log_alpha = 0;
    while 1
        log_alpha = log_alpha + 1;
        Qout = originalLGFfunction(log_alpha,p.Left.BaseLevel,p.Left.SaturationLevel,p.Left.Q);
        if (Qout < 0)
            break
        end
    end
    interval = [(log_alpha - 1)  log_alpha];      
        
    % Find the zero crossing of LGF_Q_diff:
    Matlab_version = sscanf(version, '%f', 1);
    if Matlab_version <= 5.2
        fprintf('MATLAB Version is not compatible at this time.\n')
        return
    else
        opt.Display = 'off';
        opt.TolX = [];
        [log_alpha] = fzero('originalLGFfunction', interval, opt, p.Left.BaseLevel,p.Left.SaturationLevel,p.Left.Q);
    end
    
    % Output the lgf_alpha variable:
    p.Left.lgf_alpha = exp(log_alpha); 

end % end LEFT
% ---------------------------- END LEFT MAP ---------------------------- %


% ----------------------- RIGHT MAP (if exists) ------------------------ %
if (isfield(p,'Right')==1)
 if (priority == 0)
  if (realtime == 0)
    % Offline processing can buffer non-integer values for pulses per frame
    p.Right.block_size       = p.General.block_size;
    p.Right.nvects           = p.Right.pulses_per_frame_per_channel;
    p.Right.analysis_rate    = p.Right.StimulationRate;
    p.Right.block_shift      = ceil(fs/p.Right.analysis_rate);
    p.Right.analysis_rate    = round(fs/p.Right.block_shift);
    p.Right.StimulationRate  = p.Right.analysis_rate;
    p.Right.total_rate       = p.Right.analysis_rate*p.Right.Nmaxima;
    p.Right.pulses_per_frame_per_channel = ((8.0*p.Right.analysis_rate)/1000); 
    p.Right.pulses_per_frame = floor(p.Right.Nmaxima*p.Right.pulses_per_frame_per_channel);
        % CHECK OPERATING LIMITS
        [app] = checkOperatingLimits(app,MAP.Right,p.Right,1,verbose,0,'R');
        % p.Right.flag = flag;
  else
    % Realtime processing ensures INTEGER VALUES for pulses per frame
    p.Right.block_size       = p.General.block_size;
    p.Right.nvects           = p.Right.pulses_per_frame_per_channel;
    p.Right.analysis_rate    = p.Right.StimulationRate;
    p.Right.block_shift      = ceil(fs/p.Right.analysis_rate);
    p.Right.analysis_rate    = fs/p.Right.block_shift;
    p.Right.pulses_per_frame_per_channel = floor(8.0/1000*p.Right.analysis_rate); 
    % ORIGINAL: p.Right.pulses_per_frame_per_channel = floor((8.0*p.Right.analysis_rate)/1000); 
    p.Right.pulses_per_frame = (p.Right.Nmaxima*p.Right.pulses_per_frame_per_channel);
    p.Right.StimulationRate  = (p.Right.pulses_per_frame*1000)/(8*p.Right.Nmaxima);
    p.Right.total_rate       =  p.Right.analysis_rate*p.Right.Nmaxima;
        % CHECK OPERATING LIMITS
        [app] = checkOperatingLimits(app,MAP.Right,p.Right,1,verbose,0,'R');
        % p.Right.flag = flag;
  end % end realtime - on/off
 else
    % Priority is ON
    % Priority parameters to be released in Ver 2.2e
    % [p.Right] = userSpecificPriorityParameters(p.Right);
 end % end priority - on/off
 
    % Define pulses
    pwL                     = uint16(p.Right.PulseWidth); 
    p.Right.pulseWidth       = typecast(pwL,'uint8');
    ppfL                    = uint16(p.Right.pulses_per_frame); 
    p.Right.pulsesPerFrame   = typecast(ppfL,'uint8');
   if (realtime == 1)
    % Realtime processing ensures IPD < 6 us
    p.Right.interpulseDuration = p.General.frameDuration*1000/(p.Right.pulses_per_frame) - 2*p.Right.PulseWidth - p.Right.IPG - p.General.durationSYNC - p.General.additionalGap;
   else 
    % Offline processing does not ensure IPD < 6 us
    p.Right.interpulseDuration = (1e6/p.Right.total_rate) - 2*p.Right.PulseWidth - p.Right.IPG - p.General.durationSYNC - p.General.additionalGap;
   end

    % Define Pulses
    pwR                        = uint16(p.Right.PulseWidth); 
    p.Right.pulseWidth         = typecast(pwR,'uint8');
    ppfR                       = uint16(p.Right.pulses_per_frame); 
    p.Right.pulsesPerFrame     = typecast(ppfR,'uint8');
    p.Right.ncycles            = uint16((p.Right.interpulseDuration/0.1)); 
    p.Right.nRFcycles          = typecast(p.Right.ncycles,'uint8');
    % Check Implant Type
    switch (p.Right.ImplantType)
        case 'CI24RE'
            p.Right.ImplantGeneration = 'CIC4';
        case 'CI24R'
            p.Right.ImplantGeneration = 'CIC4';
        case 'CI24M'
            p.Right.ImplantGeneration = 'CIC3';
        otherwise
            disp('Right Implant: Check implant chip type');
            p.Right.ImplantGeneration = 'CIC4'; % Use default
    end
    % Implant Generation
    if strcmp(p.Right.ImplantGeneration, 'CIC4')
        switch (p.Right.StimulationMode)
            case 'MP1+2'
                p.Right.StimulationModeCode = 28; %only for CIC4 chip type
            otherwise
                disp('Right: Update stimulation mode code for the stimulation mode');
                p.Right.StimulationModeCode = 28; %MP1+2
        end
    end
    % Implant Type
    if strcmp(p.Right.ImplantGeneration, 'CIC3')
        switch (p.Right.StimulationMode)
            case 'MP1+2'
                p.Right.StimulationModeCode = 30; %only for CIC3 chip type
            otherwise
                disp('Right: Update stimulation mode code for the stimulation mode');
                p.Right.StimulationModeCode = 28; %MP1+2
        end
    end
    % Stimulation Order
    p.Right.sub_mag = p.General.sub_mag;
    switch (p.Right.ChannelOrderType)
        case 'base-to-apex'
            p.Right.channel_order = (p.Right.NumberOfBands:-1:1)';            
        case 'apex-to-base'
            p.Right.channel_order = (1:1:p.Right.NumberOfBands)';
    end
    % Frequency-Time Matrix Parameters
    p.Right.ranges      = p.Right.MCL - p.Right.THR;
    p.Right.global_gain = 1;
    p.Right.NHIST       = p.Right.block_size-p.Right.block_shift;
    p.Right.fft_size    = p.General.fft_size;
    p.Right.num_bins    = p.General.num_bins;
    p.Right.bin_freq    = p.General.bin_freq;
    p.Right.bin_freqs   = p.General.bin_freqs;
    % Window
    switch (p.Right.Window)
        case 'Hanning'
            a = [0.5, 0.5, 0.0, 0.0 ];
        case 'Hamming'
            a = [0.54, 0.46, 0.0, 0.0 ];
        case 'Blackman'
            a = [0.42, 0.5, 0.08, 0.0 ];
        otherwise
            a = [0.5, 0.5, 0.0, 0.0 ];
    end
    n = (0:p.Right.block_size-1)';		
    r = 2*pi*n/p.Right.block_size;		
    p.Right.window = a(1) - a(2)*cos(r) + a(3)*cos(2*r) - a(4)*cos(3*r); 
    % Filter Weights
    p.Right.band_bins = FFT_band_bins(p.Right.NumberOfBands)';
    % Weights matrix for combining FFT bins into bands: RIGHT
    p.Right.weights = zeros(p.Right.NumberOfBands, p.Right.num_bins);
    bin = 3;	% We always ignore bins 0 (DC) & 1.
    for band = 1:p.Right.NumberOfBands
        width = p.Right.band_bins(band);
        p.Right.weights(band, bin:(bin + width - 1)) = 1;
        bin = bin + width;
    end
        % Optionally incorporate frequency response equalisation:
        freq_response  = freqz(p.Right.window/2, 1, p.Right.block_size);
        power_response = freq_response .* conj(freq_response);
        P1 = power_response(1);
        P2 = 2 * power_response(2);
        P3 = power_response(1) + 2 * power_response(3);
    % Power Gains 
    p.Right.power_gains = zeros(p.Right.NumberOfBands, 1);
    for band = 1:p.Right.NumberOfBands
        width = p.Right.band_bins(band);
        if (width == 1)
            p.Right.power_gains(band) = P1;
        elseif (width == 2)
            p.Right.power_gains(band) = P2;
        else
            p.Right.power_gains(band) = P3;
        end
    end
    % Apply Gains to Bands
    for band = 1:p.Right.NumberOfBands
        p.Right.weights(band, :) = p.Right.weights(band, :) / p.Right.power_gains(band);
    end
    % Frequency boundries of triangular filters
    cum_num_bins            = [1.5; 1.5 + cumsum(p.Right.band_bins)];
    p.Right.crossover_freqs = cum_num_bins * p.Right.bin_freq;
    p.Right.band_widths     = diff(p.Right.crossover_freqs);
    p.Right.char_freqs      = p.Right.crossover_freqs(1:p.Right.NumberOfBands) + p.Right.band_widths/2;
    % Sensitivity
    p.Right.sensitivity     = 2.3; p.Right.scale_factor = p.Right.sensitivity/32768;
    % Gains
    p.Right.gains_dB        = p.Right.Gain+ p.Right.BandGains;
    p.Right.gains           = 10 .^ (p.Right.gains_dB/ 20.0); 
    % Volume
    p.Right.volume_level    = p.Right.Volume/10;
    % Channel Selection
    p.Right.num_rejected    = p.Right.NumberOfBands - p.Right.Nmaxima;
    
    % ORIGINAL: LGF Compression
    if (p.Right.BaseLevel > 0)
        % For information, not used in processing
        p.Right.lgf_dynamic_range = 20*log10(p.Right.SaturationLevel/p.Right.BaseLevel);
    end
    
    % --------------------------- LGF_alpha -------------------------- %
    % p.Right.lgf_alpha = LGF_alpha(p.Right.Q,p.Right.BaseLevel,p.Right.SaturationLevel);
    % alpha = LGF_alpha(Q,BaseLevel,SaturationLevel)
    log_alpha = 0;
    while 1
        log_alpha = log_alpha + 1;
        Qout = originalLGFfunction(log_alpha,p.Right.BaseLevel,p.Right.SaturationLevel,p.Right.Q);
        if (Qout < 0)
            break
        end
    end
    interval = [(log_alpha - 1)  log_alpha];      
        
    % Find the zero crossing of LGF_Q_diff:
    Matlab_version = sscanf(version, '%f', 1);
    if Matlab_version <= 5.2
        fprintf('MATLAB Version is not compatible at this time.\n')
        return
    else
        opt.Display = 'off';
        opt.TolX = [];
        [log_alpha] = fzero('originalLGFfunction', interval, opt, p.Right.BaseLevel,p.Right.SaturationLevel,p.Right.Q);
    end
    
    % Output the lgf_alpha variable:
    p.Right.lgf_alpha = exp(log_alpha); 
end % end RIGHT
% --------------------------- END RIGHT MAP ---------------------------- %

fprintf('Done Initializing.\n');

end % end of 'intialization.m'

%% -------------------- EMBEDDED FUNCTIONS ----------------------------- %
function [mod_map] = field_check(org_map)
%% Function 1: 'field_check.m'
% MAP Checking Routine
% Author: Hussnain Ali
% Date:   12/07/2010
% Edited: Juliana N. Saba
% Date:   01/03/2019
%         University of Texas at Dallas (c) Copyright 2019

mod_map     = org_map;
% Load default/standard parameters in case any field is missng from the MAP file
std_params  = default_parameters; 
left_flag   = 0; 
right_flag  = 0;
if (isfield(mod_map,'General')==0)
    mod_map.General.Comments = 'Populating Default values in General Field';
end
% Optional: Subject Name
if (isfield(mod_map.General,'SubjectName')==0)
    mod_map.General.SubjectName = 'Anonymous';
end
% Optional: SubjectID
if (isfield(mod_map.General,'SubjectID')==0)
    mod_map.General.SubjectID = 'XYZ';
end
% Optional: Map Title
if (isfield(mod_map.General,'MapTitle')==0)
    mod_map.General.MapTitle = 'XYZ01';
end
% Optional: NumberOfImplants
if (isfield(mod_map,'Left')==1)&&(isfield(mod_map,'Right')==1)
    mod_map.General.NumberOfImplants = 2;
else
    mod_map.General.NumberOfImplants = 1;
end
% Optional: Implanted Ear
if (isfield(mod_map.General,'ImplantedEar')==0)
    if (isfield(mod_map,'Left')==1)&&(isfield(mod_map,'Right')==1)
        mod_map.General.ImplantedEar = 'Bilateral';
    elseif (isfield(mod_map,'Left')==1)&&(isfield(mod_map,'Right')==0)
        mod_map.General.ImplantedEar = 'Left';
    elseif (isfield(mod_map,'Left')==0)&&(isfield(mod_map,'Right')==1)
        mod_map.General.ImplantedEar = 'Right';
    end
end
% Stimulate Ears % This field is used if you purposely would like
% to stimulate one of the two implanted ears
if (isfield(mod_map.General,'StimulateEars')==0)
    if (isfield(mod_map,'Left')==1)&&(isfield(mod_map,'Right')==1)
        mod_map.General.StimulateEars = 'Both';
    elseif (isfield(mod_map,'Left')==1)&&(isfield(mod_map,'Right')==0)
        mod_map.General.StimulateEars = 'Left';
    elseif (isfield(mod_map,'Left')==0)&&(isfield(mod_map,'Right')==1)
        mod_map.General.StimulateEars = 'Right';
    else
        mod_map.General.StimulateEars = 'NULL';
    end
end

% ------------------------------- LEFT EAR ----------------------------- %
if (isfield(mod_map,'Left')==1)
    % Implant Type - Important
    if (isfield(mod_map.Left,'ImplantType')==0)
        mod_map.Left.ImplantType = std_params.ImplantType;
        errordlg('Left: Implant Type is unknown!', 'Missing MAP info');
        disp(['Left: Missing Info - Implant Type - Default value of: ', std_params.ImplantType, ' loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Sampling Frequency
    if (isfield(mod_map.Left,'SamplingFrequency')==0)
        mod_map.Left.SamplingFrequency = std_params.SamplingFrequency;
        disp([ 'Left: Missing Info - Sampling Frequency - Default value of: ', num2str(std_params.SamplingFrequency), ' Hz loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Number of Channels
    if (isfield(mod_map.Left,'NumberOfChannels')==0)
        mod_map.Left.NumberOfChannels = std_params.NumberOfChannels;
        disp([ 'Left: Missing Info - NumberOfChannels - Default value of: ', num2str(std_params.NumberOfChannels), ' loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Strategy
    if (isfield(mod_map.Left,'Strategy')==0)
        mod_map.Left.Strategy = std_params.Strategy;
        disp([ 'Left: Missing Info - Strategy - Default strategy: ', std_params.Strategy, ' loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Nmaxima
    if (isfield(mod_map.Left,'Nmaxima')==0)
        mod_map.Left.Nmaxima = std_params.Nmaxima;
        disp([ 'Left: Missing Info - Nmaxima - Default value of: ', num2str(std_params.Nmaxima), ' loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % StimulationMode
    if (isfield(mod_map.Left,'StimulationMode')==0)
        mod_map.Left.StimulationMode = std_params.StimulationMode;
        disp([ 'Left: Missing Info - StimulationMode - Default Stimulation Mode: ', std_params.StimulationMode, ' loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % StimulationRate
    if (isfield(mod_map.Left,'StimulationRate')==0)
        mod_map.Left.StimulationRate = std_params.StimulationRate;
        disp([ 'Left: Missing Info - StimulationRate - Default value of: ', num2str(std_params.StimulationRate), ' pps loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % PulseWidth
    if (isfield(mod_map.Left,'PulseWidth')==0)
        mod_map.Left.PulseWidth = std_params.PulseWidth;
        disp([ 'Left: Missing Info - PulseWidth - Default value of: ', num2str(std_params.PulseWidth), ' us loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % IPG
    if (isfield(mod_map.Left,'IPG')==0)
        mod_map.Left.IPG = std_params.IPG;
        disp([ 'Left: Missing Info - IPG - Default value of: ', num2str(std_params.IPG), ' us loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Sensitivity
    if (isfield(mod_map.Left,'Sensitivity')==0)
        mod_map.Left.Sensitivity = std_params.Sensitivity;
        disp([ 'Left: Missing Info - Sensitivity - Default value of: ', num2str(std_params.Sensitivity), ' loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Gain
    if (isfield(mod_map.Left,'Gain')==0)
        mod_map.Left.Gain = std_params.Gain;
        disp([ 'Left: Missing Info - Gain - Default value of: ', num2str(std_params.Gain), ' dB loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Volume
    if (isfield(mod_map.Left,'Volume')==0)
        mod_map.Left.Volume = std_params.Volume;
        disp([ 'Left: Missing Info - Volume - Default value of: ', num2str(std_params.Volume), ' loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Q
    if (isfield(mod_map.Left,'Q')==0)
        mod_map.Left.Q = std_params.Q;
        disp([ 'Left: Missing Info - Q - Default value of: ', num2str(std_params.Q), ' loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % BaseLevel
    if (isfield(mod_map.Left,'BaseLevel')==0)
        mod_map.Left.BaseLevel = std_params.BaseLevel;
        disp([ 'Left: Missing Info - BaseLevel - Default value of: ', num2str(std_params.BaseLevel), ' loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % SaturationLevel
    if (isfield(mod_map.Left,'SaturationLevel')==0)
        mod_map.Left.SaturationLevel = std_params.SaturationLevel;
        disp([ 'Left: Missing Info - SaturationLevel - Default value of: ', num2str(std_params.SaturationLevel), ' loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % ChannelOrderType
    if (isfield(mod_map.Left,'ChannelOrderType')==0)
        mod_map.Left.ChannelOrderType = std_params.ChannelOrderType;
        disp([ 'Left: Missing Info - ChannelOrderType - Default order: ', std_params.ChannelOrderType, ' loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % FrequencyTable
    if (isfield(mod_map.Left,'FrequencyTable')==0)
        mod_map.Left.FrequencyTable = std_params.FrequencyTable;
        disp([ 'Left: Missing Info - FrequencyTable: ', std_params.FrequencyTable, '  Frequency Table loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Window
    if (isfield(mod_map.Left,'Window')==0)
        mod_map.Left.Window = std_params.Window;
        disp([ 'Left: Missing Info - Window - Default : ', std_params.Window, ' window loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % El_CF1_CF2_THR_MCL_Gain
    if (isfield(mod_map.Left,'El_CF1_CF2_THR_MCL_Gain')==0)
        mod_map.Left.El_CF1_CF2_THR_MCL_Gain = std_params.El_CF1_CF2_THR_MCL_Gain;
        disp([ 'Left: Missing Info - Electrodes_LowerCutOffFrequencies_UpperCutOffFrequencies_THR_MCL_Gain - Default values loaded']);
        errordlg('Please re-check THR and MCL values before stimulating', 'MAP ERROR');
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Final LEFT parameters
    mod_map.Left.NumberOfBands          = size(mod_map.Left.El_CF1_CF2_THR_MCL_Gain, 1);    % Number of active electrodes/bands
    mod_map.Left.Electrodes             = mod_map.Left.El_CF1_CF2_THR_MCL_Gain(:, 1);       % Active Electrodes
    mod_map.Left.LowerCutOffFrequencies = mod_map.Left.El_CF1_CF2_THR_MCL_Gain(:, 2);       % Low cut-off frequencies of filters
    mod_map.Left.UpperCutOffFrequencies = mod_map.Left.El_CF1_CF2_THR_MCL_Gain(:, 3);       % Upper cut-off frequencies of filters
    mod_map.Left.THR                    = mod_map.Left.El_CF1_CF2_THR_MCL_Gain(:, 4);       % Threshold Levels (THR)
    mod_map.Left.MCL                    = mod_map.Left.El_CF1_CF2_THR_MCL_Gain(:, 5);       % Maximum Comfort Levels (MCL)
    mod_map.Left.BandGains              = mod_map.Left.El_CF1_CF2_THR_MCL_Gain(:, 6);       % Individual Band Gains (dB)
    % Comments
    if (isfield(mod_map.Left,'Comments')==0)
        mod_map.Left.Comments = std_params.Comments;
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    if (left_flag==1)
        comments = [mod_map.Left.Comments ' | LEFT: One or more missing values were found in the MAP, which were loaded by default values'];
        mod_map.Left.Comments = comments;
    end
end % end left 

% ----------------------------- RIGHT EAR ------------------------------ %
if (isfield(mod_map,'Right')==1)
    % Implant Type - Important
    if (isfield(mod_map.Right,'ImplantType')==0)
        %mod_params.ImplantType = std_params.ImplantType;
        mod_map.Right.ImplantType = std_params.ImplantType;
        errordlg('Right: Implant Type is unknown!', 'Missing MAP info');
        disp(['Right: Missing Info - Implant Type - Default value of: ', std_params.ImplantType, ' loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Sampling Frequency
    if (isfield(mod_map.Right,'SamplingFrequency')==0)
        mod_map.Right.SamplingFrequency = std_params.SamplingFrequency;
        disp([ 'Right: Missing Info - Sampling Frequency - Default value of: ', num2str(std_params.SamplingFrequency), ' Hz loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Number of Channels
    if (isfield(mod_map.Right,'NumberOfChannels')==0)
        mod_map.Right.NumberOfChannels = std_params.NumberOfChannels;
        disp([ 'Right: Missing Info - NumberOfChannels - Default value of: ', num2str(std_params.NumberOfChannels), ' loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Strategy
    if (isfield(mod_map.Right,'Strategy')==0)
        mod_map.Right.Strategy = std_params.Strategy;
        disp([ 'Right: Missing Info - Strategy - Default strategy: ', std_params.Strategy, ' loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Nmaxima
    if (isfield(mod_map.Right,'Nmaxima')==0)
        mod_map.Right.Nmaxima = std_params.Nmaxima;
        disp([ 'Right: Missing Info - Nmaxima - Default value of: ', num2str(std_params.Nmaxima), ' loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % StimulationMode
    if (isfield(mod_map.Right,'StimulationMode')==0)
        mod_map.Right.StimulationMode = std_params.StimulationMode;
        disp([ 'Right: Missing Info - StimulationMode - Default Stimulation Mode: ', std_params.StimulationMode, ' loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % StimulationRate
    if (isfield(mod_map.Right,'StimulationRate')==0)
        mod_map.Right.StimulationRate = std_params.StimulationRate;
        disp([ 'Right: Missing Info - StimulationRate - Default value of: ', num2str(std_params.StimulationRate), ' pps loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % PulseWidth
    if (isfield(mod_map.Right,'PulseWidth')==0)
        mod_map.Right.PulseWidth = std_params.PulseWidth;
        disp([ 'Right: Missing Info - PulseWidth - Default value of: ', num2str(std_params.PulseWidth), ' us loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % IPG
    if (isfield(mod_map.Right,'IPG')==0)
        mod_map.Right.IPG = std_params.IPG;
        disp([ 'Right: Missing Info - IPG - Default value of: ', num2str(std_params.IPG), ' us loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Sensitivity
    if (isfield(mod_map.Right,'Sensitivity')==0)
        mod_map.Right.Sensitivity = std_params.Sensitivity;
        disp([ 'Right: Missing Info - Sensitivity - Default value of: ', num2str(std_params.Sensitivity), ' loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Gain
    if (isfield(mod_map.Right,'Gain')==0)
        mod_map.Right.Gain = std_params.Gain;
        disp([ 'Right: Missing Info - Gain - Default value of: ', num2str(std_params.Gain), ' dB loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Volume
    if (isfield(mod_map.Right,'Volume')==0)
        mod_map.Right.Volume = std_params.Volume;
        disp([ 'Right: Missing Info - Volume - Default value of: ', num2str(std_params.Volume), ' loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Q
    if (isfield(mod_map.Right,'Q')==0)
        mod_map.Right.Q = std_params.Q;
        disp([ 'Right: Missing Info - Q - Default value of: ', num2str(std_params.Q), ' loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % BaseLevel
    if (isfield(mod_map.Right,'BaseLevel')==0)
        mod_map.Right.BaseLevel = std_params.BaseLevel;
        disp([ 'Right: Missing Info - BaseLevel - Default value of: ', num2str(std_params.BaseLevel), ' loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % SaturationLevel
    if (isfield(mod_map.Right,'SaturationLevel')==0)
        mod_map.Right.SaturationLevel = std_params.SaturationLevel;
        disp([ 'Right: Missing Info - SaturationLevel - Default value of: ', num2str(std_params.SaturationLevel), ' loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % ChannelOrderType
    if (isfield(mod_map.Right,'ChannelOrderType')==0)
        mod_map.Right.ChannelOrderType = std_params.ChannelOrderType;
        disp([ 'Right: Missing Info - ChannelOrderType - Default order: ', std_params.ChannelOrderType, ' loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % FrequencyTable
    if (isfield(mod_map.Right,'FrequencyTable')==0)
        mod_map.Right.FrequencyTable = std_params.FrequencyTable;
        disp([ 'Right: Missing Info - FrequencyTable: ', std_params.FrequencyTable, '  Frequency Table loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Window
    if (isfield(mod_map.Right,'Window')==0)
        mod_map.Right.Window = std_params.Window;
        disp([ 'Right: Missing Info - Window - Default : ', std_params.Window, ' window loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % El_CF1_CF2_THR_MCL_Gain
    if (isfield(mod_map.Right,'El_CF1_CF2_THR_MCL_Gain')==0)
        mod_map.Right.El_CF1_CF2_THR_MCL_Gain = std_params.El_CF1_CF2_THR_MCL_Gain;
        disp([ 'Right: Missing Info - Electrodes_LowerCutOffFrequencies_UpperCutOffFrequencies_THR_MCL_Gain - Default values loaded']);
        errordlg('Please re-check THR and MCL values before stimulating', 'MAP ERROR');
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    % Final RIGHT parameters
    mod_map.Right.NumberOfBands          = size(mod_map.Right.El_CF1_CF2_THR_MCL_Gain, 1);    % Number of active electrodes/bands
    mod_map.Right.Electrodes             = mod_map.Right.El_CF1_CF2_THR_MCL_Gain(:, 1);       % Active Electrodes
    mod_map.Right.LowerCutOffFrequencies = mod_map.Right.El_CF1_CF2_THR_MCL_Gain(:, 2);       % Low cut-off frequencies of filters
    mod_map.Right.UpperCutOffFrequencies = mod_map.Right.El_CF1_CF2_THR_MCL_Gain(:, 3);       % Upper cut-off frequencies of filters
    mod_map.Right.THR                    = mod_map.Right.El_CF1_CF2_THR_MCL_Gain(:, 4);       % Threshold Levels (THR)
    mod_map.Right.MCL                    = mod_map.Right.El_CF1_CF2_THR_MCL_Gain(:, 5);       % Maximum Comfort Levels (MCL)
    mod_map.Right.BandGains              = mod_map.Right.El_CF1_CF2_THR_MCL_Gain(:, 6);       % Individual Band Gains (dB)
    % Comments
    if (isfield(mod_map.Right,'Comments')==0)
        mod_map.Right.Comments = std_params.Comments;
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    if (right_flag==1)
        comments = [mod_map.Right.Comments ' | RIGHT: One or more missing values were found in the MAP, which were loaded by default values'];
        mod_map.Right.Comments = comments;
    end
end % end RIGHT



%% %%%%%%%%%%%%%%%% EMBEDDED FUNCTION (default_parameters) %%%%%%%%%%%%%%%
    function params = default_parameters 
            params.ImplantType        = 'CI24RE';     % Implant chip type, e.g., CI24RE(CS/CA), CI24R, CI24M, CI22M, ST
            params.SamplingFrequency  = 16000;        % Fixed
            params.NumberOfChannels   = 22;           % 22 fixed for imlants from Cochlear Ltd.
            params.Strategy           = 'ACE';        % 'ACE' or 'CIS' or 'Custom'
            params.Nmaxima            = 8;            % Nmaxima 1 - 22 for n-of-m strategies
            params.StimulationMode    = 'MP1+2';      % Electrode Configuration/Stimulation mode e.g., MP1, MP1+2, BP1, BP1+2, CG,....etc.
            params.StimulationRate    = 1000;         % Stimulation rate per electrode in number of pulses per second (pps)
            params.PulseWidth         = 25;           % Pulse width in us
            params.IPG                = 8;            % Inter-Phase Gap (IPG) fixed at 8us (could be variable in future)
            params.Sensitivity        = 2.3;          % Microphone Sensitivity (adjustable in GUI)
            params.Gain               = 25;           % Global gain for envelopes in dB - standard is 25dB (adjustable in GUI)
            params.Volume             = 10;           % Volume Level on a scale of 0 to 10; 0 being lowest and 10 being highest (adjustable in GUI)
            params.Q                  = 20;           % Q-factor for the compression function
            params.BaseLevel          = 0.0156;       % Base Level
            params.SaturationLevel    = 0.5859;       % Saturation Level
            params.ChannelOrderType   = 'apex-to-base'; % Channel Stimulation Order type: 'base-to-apex' or 'apex-to-base'
            params.FrequencyTable     = 'Default';    % Frequency assignment for each band "Default" or "Custom"
            params.Window             = 'Hanning';    % Window type
            params.El_CF1_CF2_THR_MCL_Gain = [
              % El  F_Low   F_High  THR     MCL     Gain
                22  188     313     101     250     0.0
                21	313     438     100     200     0.0
                20	438     563     100     200     0.0
                19	563     688     100     200     0.0
                18	688     813     100     200     0.0
                17	813     938     100     200     0.0
                16	938     1063    100     200     0.0
                15	1063    1188    100     200     0.0
                12	1188    1313    100     200     0.0
                13	1313    1563    100     200     0.0
                12	1563    1813    100     200     0.0
                11	1813    2063    100     200     0.0
                10	2063    2313    100     200     0.0
                9	2313    2688    100     200     0.0
                8	2688    3063    100     200     0.0
                7	3063    3563    100     200     0.0
                6	3563    4063    100     200     0.0
                5	4063    4688    100     200     0.0
                4	4688    5313    100     200     0.0
                3	5313    6063    100     200     0.0
                2	6063    6938    100     200     0.0
                1	6938    7938    100     200     0.0
                ];
            params.NumberOfBands          = size(params.El_CF1_CF2_THR_MCL_Gain, 1);    % Number of active electrodes/bands
            params.Electrodes             = params.El_CF1_CF2_THR_MCL_Gain(:, 1);       % Active Electrodes
            params.LowerCutOffFrequencies = params.El_CF1_CF2_THR_MCL_Gain(:, 2);       % Low cut-off frequencies of filters
            params.UpperCutOffFrequencies = params.El_CF1_CF2_THR_MCL_Gain(:, 3);       % Upper cut-off frequencies of filters
            params.THR                    = params.El_CF1_CF2_THR_MCL_Gain(:, 4);       % Threshold Levels (THR)
            params.MCL                    = params.El_CF1_CF2_THR_MCL_Gain(:, 5);       % Maximum Comfort Levels (MCL)
            params.BandGains              = params.El_CF1_CF2_THR_MCL_Gain(:, 6);       % Individual Band Gains (dB)
            params.Comments               = '';                                         % Optional: comments
    end % end of embedded function, 'default_parameters.m'
% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

end % end 'field_check.m'




function [mod_map] = timing_check(org_map)
%% Function 2. 'timing_check.m'
% Error Checking Routine -- Parameter Checking routines for rate and 
% pulse width adjustments. Used to calculate correct number of pulse per 
% frame per channel

% Author: Hussnain Ali
% Date:   12/07/2010
% Edited: Juliana N. Saba
% Date:   01/03/2019
%         University of Texas at Dallas (c) Copyright 2019

mod_map = org_map;
% Check the LEFT parameters
if (isfield(org_map,'Left')==1)
    num_selected                 = org_map.Left.Nmaxima;
    pw                           = org_map.Left.PulseWidth;
    rate                         = org_map.Left.StimulationRate;
    ipg                          = org_map.Left.IPG;
   % Calls the function, 'check_timing_parameters.m'
    [rate_outL,pw_outL,ppfpchL]  = check_timing_parameters(num_selected,rate,pw,ipg,'L');
    mod_map.Left.StimulationRate = rate_outL;
    mod_map.Left.PulseWidth      = pw_outL;
    mod_map.Left.pulses_per_frame_per_channel = ppfpchL;
    mod_map.Left.pulses_per_frame             = ppfpchL*num_selected;
end
% Check the RIGHT parameters
if (isfield(org_map,'Right')==1)
    num_selected                  = org_map.Right.Nmaxima;
    pw                            = org_map.Right.PulseWidth;
    rate                          = org_map.Right.StimulationRate;
    ipg                           = org_map.Right.IPG;
    % Calls the function, 'check_timing_parameters.m'
    [rate_outR,pw_outR,ppfpchR]   = check_timing_parameters(num_selected,rate,pw,ipg,'R');
    mod_map.Right.StimulationRate = rate_outR;
    mod_map.Right.PulseWidth      = pw_outR;
    mod_map.Right.pulses_per_frame_per_channel = ppfpchR;
    mod_map.Right.pulses_per_frame             = ppfpchR*num_selected;
end


%% %%%%%%%%%%%%%%%%%%%%% EMBEDDED FUNCTION (function 2) %%%%%%%%%%%%%%%%%%
    function [rate_set,pw_set,pulses_per_frame_per_channel] = check_timing_parameters(num_selected,rate,pw,IPG,ear)
      % User-specified Signal Processing Parameters:
        pw_org   = pw; rate_org = rate;
      % Part 1. Check MAX Pulse Width
        if pw > 400
            pw = 400;
         % NEW! Notify the user that PW exceeds operating specifications
         %   warndlg(['Warning! User specified pulse width exceeds operating specifications. Adjusted to: ' num2str(pw)]);
        end
      % Part 2. Check MIN Pulse Width
        if pw < 25
            pw = 25;
         % NEW! Notify the user that PW does not meet operating specifications
         %   warndlg(['Warning! User specified pulse width does not meet minimum operating specifications. Adjusted to: ' num2str(pw)]);
        end
      SG = 0; AG = 1;
      % This is the maximum stimulation rate supported by CIC4 in STD mode
      Total_Stimulation_Rate = 14400; thr = Total_Stimulation_Rate; 
      % Part 3. Check Protocol (STD or HRT)
        if (num_selected*rate <= thr) && (rate <= thr)
            protocol   = 'STD'; 
            max_pw_STD = floor(1e6/(rate*num_selected) - (IPG+SG+AG));
            if pw > max_pw_STD
                pw = max_pw_STD;
              % NEW! Notify the user regarding change to pulse width
              %  warndlg(['Warning! User specified pulse width is outside operating specifications. Pulse Width Adjusted to: ' num2str(pw)]);
            end
        else
            errordlg('High Rate Protocol is currently not supported!');
            protocol = 'HRT'; % High Rate Protocol (HR)
            while (num_selected*rate>thr)
              % Decrement the rate until THR is reached
                rate = rate-1;
            end
            rate = floor(rate);
            disp('Stimulation rate was decremented manually.');
            % NEW! Notify the user regarding change to pulse width
            %    warndlg(['Warning! User specified stimulation rate has been adjusted to: ' num2str(rate)]);
        end
        
      % Part 4. Check the center pulse width and new rate
        rate = floor(rate); pw = floor(pw);
        pulses_per_frame_per_channel = ceil(8.0/1000*rate);
                    % WARNING! CCi-MOBILE ver 3 modified line!
        pulses_per_frame             = (num_selected*pulses_per_frame_per_channel); 
        
      % Part 4.5. Check integer value of ppf
        if rem(pulses_per_frame,1)~=0
            % Non-integer pulses-per-frame -- Data will be lost
            disp('Non-integer pulses per frame detected, data may be lost.');
        end
       
      % Part 5. Check stimulation mode
        % Monopolar stimulation mode
        max_pw = floor(0.5*((1e6/(rate*num_selected)) - (11+IPG)));
        % Bipolar stimulation mode
        % max_pw = floor(0.5*((1e6/(rate*num_selected)) - (11+IPG+200))); 
        
      % Part 6. Check the adjusted pulse width for stimulation mode
        if pw > max_pw
            pw_set = max_pw;
         % NEW! Notify the user regarding change to pulse width
         %   warndlg(['Warning! User specified pulse width is outside operating specifications. Pulse Width Adjusted to: ' num2str(pw)]);
        end
        
      % Part 7. Original algorithm REMOVED from this section
        rate_set = rate; 
        pw_set   = pw;
        if rate_set~=rate_org && pw_set~=pw_org
            %warndlg('Rate and Pulse Width has been adjusted','Rate and Pulse-width adjustment')
            disp(['Rate has been adjusted to: ', num2str(rate_set), ' pps']);
            disp(['Pulse Width has been adjusted to: ', num2str(pw_set), ' us']);
        elseif pw_set~=pw_org
            %warndlg('Pulse Width has been adjusted','Pulse-width adjustment')
            disp(['Pulse Width has been adjusted to: ', num2str(pw_set), ' us']);
        elseif rate_set~=rate_org
            %warndlg('Rate has been adjusted','Rate adjustment')
            disp(['Rate has been adjusted to: ', num2str(rate_set), ' pps']);
        end
    end % end of 'check_timing_parameters.m'
% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

end % function end 'timing_check.m'




function [mod_map] = level_check(org_map)
%% Function 3. 'level_check.m'
% Author: Hussnain Ali
% Date:   12/07/2010
% Edited: Juliana N. Saba
% Date:   01/03/2019
%         University of Texas at Dallas (c) Copyright 2019

mod_map = org_map;
% Check the LEFT MAP
if (isfield(org_map,'Left')==1)
    n   = org_map.Left.NumberOfBands;
    thr = org_map.Left.THR; 
    mcl = org_map.Left.MCL;
    level_check_flag1_l = 0; 
    level_check_flag2_l = 0;
    for i=1:n
        if (thr(i))<0
            fprintf('Channel %d : THR(%d) = %d -> Corrected to 0.\n',i, i, thr(i));
            thr(i)=0;
        end
        if (mcl(i)>255)
            fprintf('Channel %d : MCL(%d) = %d -> Corrected to 255.\n',i, i, mcl(i));
            mcl(i)=255;
            level_check_flag1_l = 1;
        end
        if (thr(i)>mcl(i))
            fprintf('Channel %d : THR(%d) = %d is greater than MCL(%d) = %d -> Corrected to 0.\n',i, i, thr(i), i, mcl(i));
            thr(i)=0;
            level_check_flag2_l = 1;
        end
    end
    if (level_check_flag1_l ==1)
        errordlg('Maximum Comfort Level(s) greater than 255', 'Map Error');
    end
    if (level_check_flag2_l ==1)
        errordlg('Threshold Level(s) of left ear greater than Maximum Comfort Level(s)', 'Map Error');
    end
    mod_map.Left.THR = thr;
    mod_map.Left.MCL = mcl;
end % end LEFT

% Check the RIGHT MAP
if (isfield(org_map,'Right')==1)
    n   = org_map.Right.NumberOfBands;
    thr = org_map.Right.THR; 
    mcl = org_map.Right.MCL;
    level_check_flag1_r = 0; 
    level_check_flag2_r = 0;
    for i=1:n
        if (thr(i))<0
            fprintf('Channel %d : THR(%d) = %d -> Corrected to 0.\n',i, i, thr(i));
            thr(i)=0;
        end
        if (mcl(i)>255)
            fprintf('Channel %d : MCL(%d) = %d -> Corrected to 255.\n',i, i, mcl(i));
            mcl(i)=255;
            level_check_flag1_r = 1;
        end
        if (thr(i)>mcl(i))
            fprintf('Channel %d : THR(%d) = %d is greater than MCL(%d) = %d -> Corrected to 0.\n',i, i, thr(i), i, mcl(i));
            thr(i)=0;
            level_check_flag2_r = 1;
        end
    end
    if (level_check_flag1_r ==1)
        errordlg('Maximum Comfort Level(s) greater than 255', 'Map Error');
    end
    if (level_check_flag2_r ==1)
        errordlg('Threshold Level(s) of right ear greater than Maximum Comfort Level(s)', 'Map Error');
    end
    mod_map.Right.THR = thr;
    mod_map.Right.MCL = mcl; 
end % end RIGHT

end % function end 'level_check.m'





function widths = FFT_band_bins(num_bands)
    % FFT_band_bins: calculate number of bins per band vector for FFT filterbanks.
    % function widths = FFT_band_bins(num_bands)
    % NOTE: Uses the same frequency boundaries as WinDPS ACE & CIS.
        switch num_bands
        case 22
            widths = [ 1, 1, 1, 1, 1, 1, 1,    1, 1, 2, 2, 2, 2, 3, 3, 4, 4, 5, 5, 6, 7, 8 ];% 7+15 = 22
        case 21
            widths = [ 1, 1, 1, 1, 1, 1, 1,    1, 2, 2, 2, 2, 3, 3, 4, 4, 5, 6, 6, 7, 8 ];   % 7+14 = 21
        case 20
            widths = [ 1, 1, 1, 1, 1, 1, 1,    1, 2, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 8 ];      % 7+13 = 20
        case 19
            widths = [ 1, 1, 1, 1, 1, 1, 1,    2, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 9 ];         % 7+12 = 19
        case 18
            widths = [ 1, 1, 1, 1, 1, 2,    2, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 9 ];         % 6+12 = 18
        case 17
            widths = [ 1, 1, 1, 2, 2,    2, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 9 ];         % 5+12 = 17
        case 16
            widths = [ 1, 1, 1, 2, 2,    2, 2, 2, 3, 4, 4, 5, 6, 7, 9,11 ];         % 5+11 = 16
        case 15
            widths = [ 1, 1, 1, 2, 2,    2, 2, 3, 3, 4, 5, 6, 8, 9,13 ];            % 5+10 = 15
        case 14
            widths = [ 1, 2, 2, 2,    2, 2, 3, 3, 4, 5, 6, 8, 9,13 ];            % 4+10 = 14
        case 13
            widths = [ 1, 2, 2, 2,    2, 3, 3, 4, 5, 7, 8,10,13 ];               % 4+ 9 = 13
        case 12
            widths = [ 1, 2, 2, 2,    2, 3, 4, 5, 7, 9,11,14 ];                  % 4+ 8 = 12
        case 11
            widths = [ 1, 2, 2, 2,    3, 4, 5, 7, 9,12,15 ];                  % 4+ 7 = 11
        case 10
            widths = [ 2, 2, 3,    3, 4, 5, 7, 9,12,15 ];                  % 3+ 7 = 10
        case  9
            widths = [ 2, 2, 3,    3, 5, 7, 9,13,18 ];                     % 3+ 6 =  9
        case  8
            widths = [ 2, 2, 3,    4, 6, 9,14,22 ];                        % 3+ 5 =  8
        case  7
            widths = [ 3, 4,    4, 6, 9,14,22 ];                        % 2+ 5 =  7
        case  6
            widths = [ 3, 4,    6, 9,15,25 ];                           % 2+ 4 =  6
        case  5
            widths = [ 3, 4,    8,16,31 ];                           % 2+ 3 =  5
        case  4
            widths = [ 7,    8,16,31 ];                           % 1+ 3 =  4
        case  3
            widths = [ 7,   15,40 ];                              % 1+ 2 =  3
        case  2
            widths = [ 7,   55 ];                                 % 1+ 1 =  2
        case  1
            widths = [ 62 ];                                %         1
        otherwise
            error('illegal number of bands');
        end
end % function end for 'FFT_bands.m'


%% %%%%%%%%%%%%%%%%%%%%% EMBEDDED FUNCTION (function 3) %%%%%%%%%%%%%%%%%%
% function [Qout] = originalLGFfunction(log_alpha,BaseLevel,SatLevel,Q)
% % ---------------------- LGF_Q_diff ---------------------------- %
% % Q_diff = LGF_Q_diff(log_alpha,Q,BaseLevel,SaturationLevel);
% % out    = LGF_Q_diff(log_alpha,Q,base_level,sat_level)
%     alpha = exp(log_alpha);
%     % -------------------- LGF_Q ------------------------------- %
%     % out = LGF_Q(alpha, base_level, sat_level) - Q;
%     % q = LGF_Q(alpha, base_level, sat_level)
%         mini.lgf_alpha  = alpha;
%         mini.base_level = BaseLevel;
%         mini.sat_level  = SatLevel;
%         mini.sub_mag    = 0;
%         input_level     = SatLevel/sqrt(10);
%         % ------------- LGF_proc --------------------------- %
%         % p = LGF_proc(p, input_level);
%         % p = LGF_proc(p, input_level)
%             r       = (input_level-BaseLevel)/(SatLevel-BaseLevel);
%             sat     = r > 1;
%             r(sat)  = 1;
%             sub     = r < 0;
%             r(sub)  = 0;
%             v       = log(1 + mini.lgf_alpha * r) / log(1 + mini.lgf_alpha);
%             v(sub)  = -1e-10;
%             mini    = v;
%         q    = 100 * (1 - mini);
%     Qout = q - Q;
% end % function end 'originalLGFalpha.m'
% % ---------------------------------------------------------------------- %

%% Function: LGF_alpha
% p.Left.lgf_alpha = LGF_alpha(p.Left.Q, p.Left.BaseLevel, p.Left.SaturationLevel);
% log_alpha = 0;
% while 1
%     log_alpha = log_alpha + 1;
%     Q_diff	= LGF_Q_diff(log_alpha,Q,BaseLevel,SaturationLevel);
%     if (Q_diff < 0)
%         break
%     end
% end
% interval = [(log_alpha - 1)  log_alpha];
% 
% % Find the zero crossing of LGF_Q_diff:
% Matlab_version = sscanf(version, '%f', 1);
% if Matlab_version <= 5.2
%     log_alpha = fzero('LGF_Q_diff', interval, [], 0, Q, BaseLevel, SaturationLevel);
% else
%     opt.Display = 'off';
%     opt.TolX = [];
%     log_alpha = fzero('LGF_Q_diff', interval, opt, Q, BaseLevel, SaturationLevel);
% end;
% 
% alpha = exp(log_alpha);

%% Function: LGF_Q_diff
% out = LGF_Q_diff(log_alpha, Q, base_level, sat_level)
%     alpha = exp(log_alpha);
%     out   = LGF_Q(alpha, base_level, sat_level) - Q;
%     
%% Function: LGF_Q
% q = LGF_Q(alpha, base_level, sat_level)
%     p.lgf_alpha  = alpha;
%     p.base_level = base_level;
%     p.sat_level  = sat_level;
%     p.sub_mag    = 0;
%     input_level  = sat_level/sqrt(10);	% 10 dB down from saturation
%     p            = LGF_proc(p, input_level);
%     q            = 100 * (1 - p);       % Convert to a percentage decrease

%% Function: LGF_proc
% p = LGF_proc(p, input_level)
% switch nargin
% 
% case 0	% Default parameters
% 	v = feval(mfilename, []);
% case 1	% Parameter calculations
% 	p = Ensure_field(p,'base_level',  4/256);
% 	p = Ensure_field(p,'sat_level', 150/256);
% 	p = Ensure_field(p,'Q',          20);
% 	p = Ensure_field(p,'sub_mag',    -1e-10);	
%     
% 	if (p.base_level > 0)
% 		p.lgf_dynamic_range = 20*log10(p.sat_level/p.base_level); 
% 	end;
% 	p.lgf_alpha	= LGF_alpha(p.Q, p.base_level, p.sat_level);	
% 	v = p;	
% 	
% case 2	% Processing
% 	r = (u - p.base_level)/(p.sat_level - p.base_level);
% 	sat = r > 1;
% 	r(sat) = 1;
% 	sub = r < 0;
% 	r(sub) = 0;
% 	v = log(1 + p.lgf_alpha * r) / log(1 + p.lgf_alpha);
% 	v(sub) = p.sub_mag;
% end