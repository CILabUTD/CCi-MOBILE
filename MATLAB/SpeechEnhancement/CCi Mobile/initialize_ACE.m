function p = initialize_ACE()
%   Initialization of ACE parameters from subject's MAP
%   Processing parameters are derived from subject's Left and Right maps

%global p;
global fs; fs = 16000;

% load map
p = load_map; %load map parameters

p.General.block_size = 128;
p.General.frameDuration = 8;
p.General.durationSYNC = 6;
p.General.additionalGap = 1;
% % % p.General.BaseLevel =  4/256;
% % % p.General.SaturationLevel = 150/256;
% % % p.General.sub_mag=-1e-10;
p.General.fft_size = 128;
p.General.num_bins		= p.General.block_size/2 + 1;
p.General.bin_freq		= fs/p.General.block_size;
p.General.bin_freqs		= p.General.bin_freq * (0:p.General.num_bins-1);
p.General.sub_mag       =    -1e-10;

%% If LEFT MAP exists
if (isfield(p,'Left')==1)
    p.Left.block_size = p.General.block_size;
    p.Left.nvects = p.Left.pulses_per_frame_per_channel;
    p.Left.analysis_rate = p.Left.StimulationRate;
    p.Left.block_shift	= ceil(fs / p.Left.analysis_rate);
    %p.Left.block_shift = ceil(p.General.block_size/p.Left.pulses_per_frame_per_channel);
    p.Left.analysis_rate=round(fs/p.Left.block_shift);
    p.Left.StimulationRate = p.Left.analysis_rate;
    p.Left.total_rate = p.Left.analysis_rate*p.Left.Nmaxima;
    
    p.Left.pulses_per_frame_per_channel=((8.0*p.Left.analysis_rate)/1000); %floor(0.5+floor((8.0*rate_set)/1000));
    p.Left.pulses_per_frame= floor(p.Left.Nmaxima*p.Left.pulses_per_frame_per_channel);
    
    pwL = uint16(p.Left.PulseWidth); p.Left.pulseWidth= typecast(pwL,'uint8');
    ppfL = uint16(p.Left.pulses_per_frame); p.Left.pulsesPerFrame = typecast(ppfL,'uint8');
    
    %p.Left.interpulseDuration = p.General.frameDuration*1000/(p.Left.pulses_per_frame) - 2*p.Left.PulseWidth - p.Left.IPG - p.General.durationSYNC - p.General.additionalGap;
    p.Left.interpulseDuration = (1e6/p.Left.total_rate) - 2*p.Left.PulseWidth - p.Left.IPG - p.General.durationSYNC - p.General.additionalGap;
    
    p.Left.ncycles = uint16((p.Left.interpulseDuration/0.1));%uint16((p.Left.interpulseDuration/0.1) + 0.5);
    p.Left.nRFcycles= typecast(p.Left.ncycles,'uint8');
    
    switch (p.Left.ImplantType)
        case 'CI24RE'
            p.Left.ImplantGeneration = 'CIC4';
        case 'CI24R'
            p.Left.ImplantGeneration = 'CIC4';
        case 'CI24M'
            p.Left.ImplantGeneration = 'CIC3';
        otherwise
            disp('Left Implant: Check implant chip type');
            p.Left.ImplantGeneration = 'CIC4'; % Use default
    end
    
    if strcmp(p.Left.ImplantGeneration, 'CIC4')
        switch (p.Left.StimulationMode)
            case 'MP1+2'
                p.Left.StimulationModeCode = 28; %only for CIC4 chip type
            otherwise
                disp('Update stimulation mode code for the stimulation mode');
                p.Left.StimulationModeCode = 28; %MP1+2
        end
    end
    
    if strcmp(p.Left.ImplantGeneration, 'CIC3')
        switch (p.Left.StimulationMode)
            case 'MP1+2'
                p.Left.StimulationModeCode = 30; %only for CIC3 chip type
            otherwise
                disp('Update stimulation mode code for the stimulation mode');
                p.Left.StimulationModeCode = 28; %MP1+2
        end
    end
    
    % p.Left.SaturationLevel = 150/256; %0.5778; %19200;%18930;%corresponds to 0.5778, 19200 corresponds to 0.5859
    % p.Left.BaseLevel = 4/256; % 0.0064; %510; %210; %corresponds to 0.0064, 510 corresponds to 0.0156 800
    p.Left.sub_mag=    p.General.sub_mag;
    %p.Left.channel_order	= 23 - (1:1:22);
    switch (p.Left.ChannelOrderType)
        case 'base-to-apex'
            p.Left.channel_order = (p.Left.NumberOfBands:-1:1)'; % frequency bands from 1 - 22
            %p.Left.electrode_order = 23 - (p.Left.Electrodes); % Cochlear Electrode terminology 22 is the lowest freq. electrode
        case 'apex-to-base'
            p.Left.channel_order = (1:1:p.Left.NumberOfBands)';
            %p.Left.electrode_order = p.Left.Electrodes;
    end
    
    p.Left.ranges = p.Left.MCL - p.Left.THR;
    
    p.Left.global_gain = 1; %25; %60 %68
    p.Left.NHIST = p.Left.block_size-p.Left.block_shift;
    
    p.Left.fft_size = p.General.fft_size;
    p.Left.num_bins = p.General.num_bins;
    p.Left.bin_freq = p.General.bin_freq;
    p.Left.bin_freqs = p.General.bin_freqs;
    
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
    n = (0:p.Left.block_size-1)';		% Time index vector.
    r = 2*pi*n/p.Left.block_size;		% Angle vector (in radians).
    p.Left.window = a(1) - a(2)*cos(r) + a(3)*cos(2*r) - a(4)*cos(3*r); %w=blackman(p.Left.block_size);
    
    % Filter Weights
    p.Left.band_bins = FFT_band_bins(p.Left.NumberOfBands)';
    % Weights matrix for combining FFT bins into bands: LEFT
    p.Left.weights = zeros(p.Left.NumberOfBands, p.Left.num_bins);
    bin = 3;	% We always ignore bins 0 (DC) & 1.
    for band = 1:p.Left.NumberOfBands;
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
    
    p.Left.power_gains = zeros(p.Left.NumberOfBands, 1);
    for band = 1:p.Left.NumberOfBands;
        width = p.Left.band_bins(band);
        if (width == 1)
            p.Left.power_gains(band) = P1;
        elseif (width == 2)
            p.Left.power_gains(band) = P2;
        else
            p.Left.power_gains(band) = P3;
        end
    end
    
    for band = 1:p.Left.NumberOfBands;
        p.Left.weights(band, :) = p.Left.weights(band, :) / p.Left.power_gains(band);
    end
    
    % Frequency boundaries of triangular filters
    cum_num_bins = [1.5; 1.5 + cumsum(p.Left.band_bins)];
    p.Left.crossover_freqs = cum_num_bins * p.Left.bin_freq;
    p.Left.band_widths = diff(p.Left.crossover_freqs);
    p.Left.char_freqs = p.Left.crossover_freqs(1:p.Left.NumberOfBands) + p.Left.band_widths/2;
    
    % sensitivity
    p.Left.sensitivity = 2.3; p.Left.scale_factor = p.Left.sensitivity/32768;
    % gains
    p.Left.gains_dB = p.Left.Gain+ p.Left.BandGains;
    p.Left.gains = 10 .^ (p.Left.gains_dB / 20.0);  %p.Left.gains(23-p.Left.off_electrodes)=[];
    % volume
    p.Left.volume_level = p.Left.Volume/10;
    
    p.Left.num_rejected = p.Left.NumberOfBands - p.Left.Nmaxima;
    % compression
    if (p.Left.BaseLevel > 0)
        % for information, not used in processing.
        p.Left.lgf_dynamic_range = 20*log10(p.Left.SaturationLevel/p.Left.BaseLevel);
    end;
    p.Left.lgf_alpha	= LGF_alpha(p.Left.Q, p.Left.BaseLevel, p.Left.SaturationLevel);
    %p.lgf_alpha = 61.4693;
end


%% If RIGHT MAP exists
if (isfield(p,'Right')==1)
    p.Right.block_size = p.General.block_size;
    p.Right.nvects = p.Right.pulses_per_frame_per_channel;
    p.Right.analysis_rate = p.Right.StimulationRate;
    p.Right.block_shift = ceil(fs/p.Right.StimulationRate);
    %p.Right.block_shift = ceil(p.General.block_size/p.Right.pulses_per_frame_per_channel);
    p.Right.analysis_rate=round(fs/p.Right.block_shift);
    p.Right.StimulationRate = p.Right.analysis_rate;
    p.Right.total_rate = p.Right.analysis_rate*p.Right.Nmaxima;

    p.Right.pulses_per_frame_per_channel=((8.0*p.Right.analysis_rate)/1000); %floor(0.5+floor((8.0*rate_set)/1000));
    p.Right.pulses_per_frame= floor(p.Right.Nmaxima*p.Right.pulses_per_frame_per_channel);
    
    pwR = uint16(p.Right.PulseWidth); p.Right.pulseWidth= typecast(pwR,'uint8');
    ppfR = uint16(p.Right.pulses_per_frame); p.Right.pulsesPerFrame = typecast(ppfR,'uint8');
    
    % timing
    %p.Right.interpulseDuration = p.General.frameDuration*1000/(p.Right.pulses_per_frame) - 2*p.Right.PulseWidth - p.Right.IPG - p.General.durationSYNC - p.General.additionalGap;
    p.Right.interpulseDuration = (1e6/p.Right.total_rate) - 2*p.Right.PulseWidth - p.Right.IPG - p.General.durationSYNC - p.General.additionalGap;
    p.Right.ncycles = uint16((p.Right.interpulseDuration/0.1)); %uint16((p.Right.interpulseDuration/0.1) + 0.5);
    p.Right.nRFcycles= typecast(p.Right.ncycles,'uint8');
    
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
    
    if strcmp(p.Right.ImplantGeneration, 'CIC4')
        switch (p.Right.StimulationMode)
            case 'MP1+2'
                p.Right.StimulationModeCode = 28; %only for CIC4 chip type
            otherwise
                disp('Right: Update stimulation mode code for the stimulation mode');
                p.Right.StimulationModeCode = 28; %MP1+2
        end
    end
    
    if strcmp(p.Right.ImplantGeneration, 'CIC3')
        switch (p.Right.StimulationMode)
            case 'MP1+2'
                p.Right.StimulationModeCode = 30; %only for CIC3 chip type
            otherwise
                disp('Right: Update stimulation mode code for the stimulation mode');
                p.Right.StimulationModeCode = 28; %MP1+2
        end
    end
    
    % p.Right.SaturationLevel = 0.5778; %19200;%18930;%corresponds to 0.5778, 19200 corresponds to 0.5859
    % p.Right.BaseLevel = 0.0064; %510; %210; %corresponds to 0.0064, 510 corresponds to 0.0156 800
    
    p.Right.sub_mag=    p.General.sub_mag;
    switch (p.Right.ChannelOrderType)
        case 'base-to-apex'
            p.Right.channel_order = (p.Right.NumberOfBands:-1:1)'; % frequency bands from 1 - 22
            %p.Right.electrode_order = 23 - (p.Right.Electrodes); % Cochlear Electrode terminology 22 is the lowest freq. electrode
        case 'apex-to-base'
            p.Right.channel_order = (1:1:p.Right.NumberOfBands)';
            %p.Right.electrode_order = p.Right.Electrodes;
    end
    
    p.Right.ranges = p.Right.MCL - p.Right.THR;
    
    p.Right.global_gain = 1; %25; %60 %68
    p.Right.NHIST = p.Right.block_size-p.Right.block_shift;
    
    p.Right.fft_size = p.General.fft_size
    p.Right.num_bins = p.General.num_bins;
    p.Right.bin_freq = p.General.bin_freq;
    p.Right.bin_freqs = p.General.bin_freqs;
    
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
    n = (0:p.Right.block_size-1)';		% Time index vector.
    r = 2*pi*n/p.Right.block_size;		% Angle vector (in radians).
    p.Right.window = a(1) - a(2)*cos(r) + a(3)*cos(2*r) - a(4)*cos(3*r); %w=blackman(p.Right.block_size);
    
    % Filter Weights
    p.Right.band_bins = FFT_band_bins(p.Right.NumberOfBands)';
    % Weights matrix for combining FFT bins into bands: Right
    p.Right.weights = zeros(p.Right.NumberOfBands, p.Right.num_bins);
    bin = 3;	% We always ignore bins 0 (DC) & 1.
    for band = 1:p.Right.NumberOfBands;
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
    
    p.Right.power_gains = zeros(p.Right.NumberOfBands, 1);
    for band = 1:p.Right.NumberOfBands;
        width = p.Right.band_bins(band);
        if (width == 1)
            p.Right.power_gains(band) = P1;
        elseif (width == 2)
            p.Right.power_gains(band) = P2;
        else
            p.Right.power_gains(band) = P3;
        end
    end
    
    for band = 1:p.Right.NumberOfBands;
        p.Right.weights(band, :) = p.Right.weights(band, :) / p.Right.power_gains(band);
    end
    
    cum_num_bins = [1.5; 1.5 + cumsum(p.Right.band_bins)];
    p.Right.crossover_freqs = cum_num_bins * p.Right.bin_freq;
    p.Right.band_widths = diff(p.Right.crossover_freqs);
    p.Right.char_freqs = p.Right.crossover_freqs(1:p.Right.NumberOfBands) + p.Right.band_widths/2;
    
    % sensitivity
    p.Right.sensitivity = 2.3; p.Right.scale_factor = p.Right.sensitivity/32768;
    % gains
    p.Right.gains_dB = p.Right.Gain+ p.Right.BandGains;
    p.Right.gains = 10 .^ (p.Right.gains_dB/ 20.0);  %p.Right.gains(23-p.Right.off_electrodes)=[];
    % volume
    p.Right.volume_level = p.Right.Volume/10;
    
    p.Right.num_rejected = p.Right.NumberOfBands - p.Right.Nmaxima;
    
    % compression
    if (p.Right.BaseLevel > 0)
        % for information, not used in processing.
        p.Right.lgf_dynamic_range = 20*log10(p.Right.SaturationLevel/p.Right.BaseLevel);
    end;
    p.Right.lgf_alpha	= LGF_alpha(p.Right.Q, p.Right.BaseLevel, p.Right.SaturationLevel);
    %p.lgf_alpha = 61.4693;
end



function alpha = LGF_alpha(Q, BaseLevel, SaturationLevel)
% LGF_alpha: Calculate Loudness Growth Function alpha factor.
% function alpha = LGF_alpha(Q, BaseLevel, SaturationLevel, fzero_options)
% This process is equivalent to an inverse, however the
% LGF function is transcendental and so this direct inverse is not possible.
% Warning: the while loop may not terminate for unusual input values.
% Find an interval that contains a zero crossing of LGF_Q_diff.
% fzero works much better if we give it this interval.
% We start with log_alpha chosen to give a positive value of LGF_Q_diff
% for sensible values of Q, BaseLevel, SaturationLevel,
% and then increment it until we see a sign change.
% We use log_alpha instead of alpha to make the search easier:
% a plot of Q vs log(alpha) changes much more smoothly than Q vs alpha.

log_alpha = 0;
while 1
    log_alpha = log_alpha + 1;
    Q_diff	= LGF_Q_diff(log_alpha, Q, BaseLevel, SaturationLevel);
    if (Q_diff < 0)
        break;
    end;
end;
interval = [(log_alpha - 1)  log_alpha];

% Find the zero crossing of LGF_Q_diff:
Matlab_version = sscanf(version, '%f', 1);
if Matlab_version <= 5.2
    log_alpha = fzero('LGF_Q_diff', interval, [], 0, Q, BaseLevel, SaturationLevel);
else
    opt.Display = 'off';
    opt.TolX = [];
    log_alpha = fzero('LGF_Q_diff', interval, opt, Q, BaseLevel, SaturationLevel);
end;

alpha = exp(log_alpha);



function widths = FFT_band_bins(num_bands)
% FFT_band_bins: calculate number of bins per band vector for FFT filterbanks.
% function widths = FFT_band_bins(num_bands)
% Uses the same frequency boundaries as WinDPS ACE & CIS.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
global fs;
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
end;




