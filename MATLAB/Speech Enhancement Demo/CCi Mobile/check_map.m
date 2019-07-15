function [map_modified return_status] = check_map(original_map)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Funtion check_map reads the map passed into its input and calls the
% functions timing_check to check for rate and pulse_width consistency.
% and function level_checks to check any errors in MCL and THR values
% Input  : original_map    % Original Patient map
% Output : map_modified    % adjusted/modified patient map
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%             Author: Hussnain Ali
%               Date: 12/08/10
% University of Texas at Dallas (c) Copyright 2010
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Call parm_check routine here
field_checked_map = field_check(original_map);
[map_timing_checked] = timing_check(field_checked_map);
[map_modified] = level_check(map_timing_checked);

end