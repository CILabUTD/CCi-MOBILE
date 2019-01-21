%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% ACE_Process
% derived from Nucleus MATLAB Toolbox v2
% Copyrights Cochlear Limited
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function stimulus = ACE_Process(x, p)
global fs; fs = p.SamplingFrequency; % 16000 Hz

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Front-end scaling and pre-emphasis
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
p.front_end_scaling = 1.0590e+003; p.input_scaling =  5.5325e-004;
%Preemphasis
p.pre_numer =    [0.5006   -0.5006];
p.pre_denom =    [1.0000   -0.0012];

y = x * p.front_end_scaling;
z = filter(p.pre_numer, p.pre_denom, y);
x = z * p.input_scaling;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% FFT_filterbank_proc: Quadrature FIR filterbank implemented with FFT.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
u = buffer(x, p.block_size, p.block_size - p.block_shift, []);
v = u .* repmat(p.window, 1, size(u,2));	% Apply window
u = fft(v);									% Perform FFT to give Frequency-Time Matrix
u(p.num_bins+1:end,:) = [];					% Discard the symmetric bins.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Power_sum_envelope_proc: Power-sum envelopes for FFT filterbank.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
v = u .* conj(u);						% Power (magnitude squared) of each bin.
u = p.weights * v;						% Weighted sum of bin powers.
u = sqrt(u);							% Magnitude.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Gain_proc: Apply gain in dB for each channel
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
v = u .* repmat(p.gains, 1, size(u,2));

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Reject_smallest_amplitude channels
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[num_bands num_time_slots] = size(v);
if (p.num_rejected > 0)
    if (p.num_rejected > num_bands)
        error('Number to be rejected is greater than number of bands');
    end
    % If we treat the input matrix v as one long column vector,
    % then the indexes of the start of each column are:
    x0 = num_bands * [0:(num_time_slots-1)];
    for n = 1:p.num_rejected
        [m k] = min(v);
        % m is x row vector containing the minimum of each column (time-slot).
        % k is x row vector containing the row number (channel number) of each minimum.
        % If we treat the input matrix v as one long column vector,
        % then the indexes of the minima are:
        xk = x0 + k;
        v(xk) = NaN;	% Reject the smallest values.
    end
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Loudness Growth Function
% function [v, sub, sat] = LGF_proc(p, u)
% v:            Magnitude in range 0:1 (proportion of dynamic range).
% sub:          Logical FTM indicating the values that were below base_level.
% sat:          Logical FTM indicating the values that were above sat_level.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[v, sub, sat] = logarithmic_compression(p, v);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Collate_into_sequence: creates a chan-mag sequence from an FTM.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[num_bands num_time_slots] = size(v);
cseq.channels = repmat(p.channel_order, num_time_slots, 1);
reord_env = v(p.channel_order,:);			% Re-order the channels (rows)
cseq.magnitudes = reord_env(:);				% Concatenate columns.

skip = isnan(cseq.magnitudes);
cseq.channels  (skip) = [];
cseq.magnitudes(skip) = [];

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Channel_mapping: Map a channel-magnitude sequence to a pulse sequence.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

if ~all(cseq.channels >= 0) | ~all(cseq.channels <= 22)
    error('Channel number out of range');
end;

electrodes			= [p.Electrodes; 0];
threshold_levels	= [p.THR; 0];
comfort_levels		= [p.MCL; 0];

idle_pulses = (cseq.channels == 0);
cseq.channels(idle_pulses) = length(electrodes);

% Create the fields in the order we like to show them in Disp_sequence:
q.electrodes	= electrodes(cseq.channels);
q.modes			= p.StimulationModeCode;

% Current level:
ranges			= comfort_levels - threshold_levels;
q_magnitudes	= cseq.magnitudes;
q_magnitudes	= min(q_magnitudes, 1.0);
q_t = threshold_levels(cseq.channels);
q_r = ranges(cseq.channels);
q.current_levels = round(q_t + q_r .* p.volume_level .* q_magnitudes);

% Idle pulses are marked by magnitudes less than zero.
q_is_idle = (q_magnitudes < 0);		% logical vector
% The current levels calculated above do not apply for idle pulses,
% set idle pulses current level to zero:
q.current_levels(q_is_idle)	= 0;
%q.electrodes(q_is_idle) = 0;

q.phase_widths		= p.PulseWidth;		% Constant phase width.
q.phase_gaps		= p.IPG;			% Constant phase gap.
% period_cycles		= round(5e6 /(p.StimulationRate*p.Nmaxima));
% q.periods			= 1e6 * period_cycles / 5e6;	% microseconds
% q.periods         = ((8e6/p.pulses_per_frame)-2*p.PulseWidth - p.IPG)/1000;
q.periods           = 1e6/(p.StimulationRate*p.Nmaxima);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Output Stimulus sequence
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

stimulus.current_levels=q.current_levels;
stimulus.electrodes= q.electrodes; 
        
%stimulus.header=['Created using ' guiInputData.Strategy ' strategy from MATLAB for CCIMobile on ' datestr(now)];
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%% Plot Electrodogram
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
q.electrodes(q_is_idle) = 0;
plot_electrodogram(q,['Electrodogram: ' p.lr_select ' ear']);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
end

