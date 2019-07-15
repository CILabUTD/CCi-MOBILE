function [v, sub, sat] = logarithmic_compression(p, u)

% LGF_proc: Loudness Growth Function
% function [v, sub, sat] = LGF_proc(p, u)
%
% Inputs:
% p:            Parameter struct, with the following fields:
% p.Q:            Percentage decrease of output when input is 10 dB below SaturationLevel.
% p.lgf_alpha:    Curve shape factor.
% p.BaseLevel:   Input value which maps to 0.
% p.SaturationLevel:    Input value which maps to 1.
% p.sub_mag:      Output value used for inputs less than BaseLevel (negative or zero).
% u:            Input magnitude vector or FTM.
%
% Outputs:
% v:            Magnitude in range 0:1 (proportion of dynamic range).
% sub:          Logical FTM indicating the values that were below BaseLevel.
% sat:          Logical FTM indicating the values that were above SaturationLevel.

       % Scale the input between BaseLevel and SaturationLevel:
        r = (u - p.BaseLevel)/(p.SaturationLevel - p.BaseLevel);
        % Find all the inputs that are above SaturationLevel (i.e. r > 1)
        % and move them down to SaturationLevel:
        sat = r > 1;		% This is a logical matrix, same size as r.
        r(sat) = 1;
        % Find all the inputs that are below BaseLevel (i.e. r < 0)
        % and temporarily move them up to BaseLevel:
        sub = r < 0;		% This is a logical matrix, same size as r.
        r(sub) = 0;
        % Logarithmic compression:
        v = log(1 + p.lgf_alpha * r) / log(1 + p.lgf_alpha);
        % Handle values that were below BaseLevel:
        v(sub) = p.sub_mag;
