function [ stimulus ] = ACE_Processing_Realtime( audio, bufferHistory, p )
%UNTITLED4 Summary of this function goes here
%   Detailed explanation goes here


    %u = buffer(audio, p.block_size, p.block_size - p.block_shift, bufferHistory);
    [u,z,opt] = buffer(audio, p.block_size, p.block_size - p.block_shift, bufferHistory);
    v = u .* repmat(p.window, 1, size(u,2));	% Apply window
    u = fft(v);									% Perform FFT to give Frequency-Time Matrix
    u(p.num_bins+1:end,:) = [];					% Discard the symmetric bins.
    v = u .* conj(u);						% Power (magnitude squared) of each bin.
    u = p.weights * v;						% Weighted sum of bin powers.
    u = sqrt(u);							% Magnitude.
    v = u .* repmat(p.gains, 1, size(u,2));
    [num_bands num_time_slots] = size(v);
    x0 = num_bands * [0:(num_time_slots-1)];
    for n = 1: p.num_rejected
        [m k] = min(v);
        xk = x0 + k;
        v(xk) = NaN;	% Reject the smallest values.
    end;
    
    [v, sub, sat] = logarithmic_compression(p, v);
    cseq.channels = repmat(p.channel_order, num_time_slots, 1);
    
    reord_env = v(p.channel_order,:);			% Re-order the channels (rows).
    %reord_env = v((num_bands:-1:1)',:);			% Re-order the channels (rows)..
    cseq.magnitudes = reord_env(:);					% Concatenate columns.
    
    skip = isnan(cseq.magnitudes);
    cseq.channels  (skip) = [];
    cseq.magnitudes(skip) = [];
    
    electrodes			= [p.Electrodes; 0]; 
    threshold_levels	= [p.THR; 0]; 
    comfort_levels		= [p.MCL; 0]; 
    
    idle_pulses = (cseq.channels == 0);
    cseq.channels(idle_pulses) = length(electrodes);
    
    % Create the fields in the order we like to show them in Disp_sequence:
    q.electrodes	= electrodes(cseq.channels);
    q.modes			= p.StimulationModeCode;					% Constant mode.
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
    % If special_idle, then also set the idle pulses electrode to zero:
    %q.electrodes(q_is_idle) = 0;
    q.phase_widths		= 28;           % Constant phase width.
    q.phase_gaps		= 8;			% Constant phase gap.
    q.periods			= 125;			% Copy periods.


    %% Modification pertinent to Streaming
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    stimulus.current_levels=q.current_levels;
    stimulus.electrodes= q.electrodes; 
end

