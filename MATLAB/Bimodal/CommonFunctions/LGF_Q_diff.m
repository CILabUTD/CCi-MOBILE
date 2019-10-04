function out = LGF_Q_diff(log_alpha, Q, base_level, sat_level)

% LGF_Q_diff: Used in LGF calculations as the function for fzero.
%
% fzero detects the zero crossing of a function as the first argument is varied. 
% This function returns the difference between
% the specified Q and the Q calculated from log_alpha, base_level, sat_level.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%    Copyright: Cochlear Ltd
%     $Archive: /SPrint Research Software/Brett2/Matlab/LoudnessGrowth/LGF_Q_diff.m $
%    $Revision: 2 $
%        $Date: 24/09/02 9:40a $
%      Authors: Brett Swanson
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

alpha = exp(log_alpha);
out = LGF_Q(alpha, base_level, sat_level) - Q;
end

function q = LGF_Q(alpha, base_level, sat_level)

% LGF_Q: Calculate Loudness Growth Function Q factor.
%
% function q = LGF_Q(alpha, base_level, sat_level)
%
% Inputs:
% alpha			- curve shape factor.
% base_level	- amplitudes below this give 0 output).
% sat_level		- input amplitude at which output saturation occurs.
%
% Output:
% q				- the percentage decrease in the output for a 10 dB decrease
%				  in the input below sat_level

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%    Copyright: Cochlear Ltd
%     $Archive: /SPrint Research Software/Latest/Matlab/LoudnessGrowth/LGF_Q.m $
%    $Revision: 5 $
%        $Date: 18/03/02 11:31a $
%      Authors: Brett Swanson
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

p.lgf_alpha  = alpha;
p.base_level = base_level;
p.sat_level  = sat_level;
p.sub_mag    = 0;
input_level = sat_level/sqrt(10);	% 10 dB down from saturation
p = LGF_proc(p, input_level);
q = 100 * (1 - p);	% Convert to a percentage decrease
end

function [v, sub, sat] = LGF_proc(p, u)

% LGF_proc: Loudness Growth Function
% function [v, sub, sat] = LGF_proc(p, u)
%
% Inputs:
% p:            Parameter struct, with the following fields:
% p.Q:            Percentage decrease of output when input is 10 dB below sat_level.
% p.lgf_alpha:    Curve shape factor.
% p.base_level:   Input value which maps to 0.
% p.sat_level:    Input value which maps to 1.
% p.sub_mag:      Output value used for inputs less than base_level (negative or zero).
% u:            Input magnitude vector or FTM.
%
% Outputs:
% v:            Magnitude in range 0:1 (proportion of dynamic range).
% sub:          Logical FTM indicating the values that were below base_level.
% sat:          Logical FTM indicating the values that were above sat_level.

switch nargin

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
case 0	% Default parameters
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	v = feval(mfilename, []);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
case 1	% Parameter calculations
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	% Defaults:

	p = Ensure_field(p,'base_level',  4/256);
	p = Ensure_field(p,'sat_level', 150/256);
	p = Ensure_field(p,'Q',          20);
	p = Ensure_field(p,'sub_mag',    -1e-10);

	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	% Derived parameters:
	
	if (p.base_level > 0)
		% for information, not used in processing.
		p.lgf_dynamic_range = 20*log10(p.sat_level/p.base_level); 
	end;
	p.lgf_alpha	= LGF_alpha(p.Q, p.base_level, p.sat_level);	
	
	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	v = p;	% Return parameters.	
	
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
case 2	% Processing
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	% Scale the input between base_level and sat_level:
	r = (u - p.base_level)/(p.sat_level - p.base_level);

	% Find all the inputs that are above sat_level (i.e. r > 1) 
	% and move them down to sat_level:
	sat = r > 1;		% This is a logical matrix, same size as r. 
	r(sat) = 1;

	% Find all the inputs that are below base_level (i.e. r < 0) 
	% and temporarily move them up to base_level:
	sub = r < 0;		% This is a logical matrix, same size as r.
	r(sub) = 0;

	% Logarithmic compression:
	v = log(1 + p.lgf_alpha * r) / log(1 + p.lgf_alpha);

	% Handle values that were below base_level:
	v(sub) = p.sub_mag;
	
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
end;
end
