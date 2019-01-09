function p = load_map
% Load map file from directory

[map_filename, map_pathname] = uigetfile('*.m', 'Select a patient map file');
if isequal(map_filename,0)
    disp('Please load a patient map file before proceeding')
else
    disp(['Map file loaded is: ', fullfile(map_pathname, map_filename)])
end

%% Step 1: Read Patient Map file
map_address=[map_pathname map_filename];
run(map_address);

%% Step 2: Check MAP for any errors, rate, pulse width, THR, and MCL values are checked here
p = check_map(MAP);