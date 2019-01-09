% MAP Checking Routine
% Author: Hussnain Ali
% Date:   12/07/2010
%         University of Texas at Dallas (c) Copyright 2010
%
% Checks if the MAP has all the fields
% If any field is missing, default parameters are loaded and user is
% informed
function [mod_map] = field_check(org_map)
% global mod_map;
mod_map=org_map;
std_params = default_parameters; % Load default/standard parameters in case any field is missng from the MAP file
left_flag = 0; right_flag = 0;

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

%% LEFT EAR
if (isfield(mod_map,'Left')==1)
    % check left parameters
    % Implant Type - Important
    if (isfield(mod_map.Left,'ImplantType')==0)
        %mod_params.ImplantType = std_params.ImplantType;
        mod_map.Left.ImplantType = std_params.ImplantType;
        errordlg('Left: Implant Type is unknown!', 'Missing MAP info');
        disp(['Left: Missing Info - Implant Type - Default value of: ', std_params.ImplantType, ' loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    
    %Sampling Frequency
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
    
    %Strategy
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
    
    %StimulationMode
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
    
    %ChannelOrderType
    if (isfield(mod_map.Left,'ChannelOrderType')==0)
        mod_map.Left.ChannelOrderType = std_params.ChannelOrderType;
        disp([ 'Left: Missing Info - ChannelOrderType - Default order: ', std_params.ChannelOrderType, ' loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    
    %FrequencyTable
    if (isfield(mod_map.Left,'FrequencyTable')==0)
        mod_map.Left.FrequencyTable = std_params.FrequencyTable;
        disp([ 'Left: Missing Info - FrequencyTable: ', std_params.FrequencyTable, '  Frequency Table loaded']);
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    
    %Window
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
    
    mod_map.Left.NumberOfBands          = size(mod_map.Left.El_CF1_CF2_THR_MCL_Gain, 1);    % Number of active electrodes/bands
    mod_map.Left.Electrodes             = mod_map.Left.El_CF1_CF2_THR_MCL_Gain(:, 1);       % Active Electrodes
    mod_map.Left.LowerCutOffFrequencies = mod_map.Left.El_CF1_CF2_THR_MCL_Gain(:, 2);       % Low cut-off frequencies of filters
    mod_map.Left.UpperCutOffFrequencies = mod_map.Left.El_CF1_CF2_THR_MCL_Gain(:, 3);       % Upper cut-off frequencies of filters
    mod_map.Left.THR                    = mod_map.Left.El_CF1_CF2_THR_MCL_Gain(:, 4);       % Threshold Levels (THR)
    mod_map.Left.MCL                    = mod_map.Left.El_CF1_CF2_THR_MCL_Gain(:, 5);       % Maximum Comfort Levels (MCL)
    mod_map.Left.BandGains              = mod_map.Left.El_CF1_CF2_THR_MCL_Gain(:, 6);       % Individual Band Gains (dB)
    
    %Comments
    if (isfield(mod_map.Left,'Comments')==0)
        mod_map.Left.Comments = std_params.Comments;
        left_flag = 1; % Flag is set to 1 if any default value is used
    end
    
    if (left_flag==1)
        comments = [mod_map.Left.Comments ' | LEFT: One or more missing values were found in the MAP, which were loaded by default values'];
        mod_map.Left.Comments = comments;
    end
    
    
    %% RIGHT EAR
    if (isfield(mod_map,'Right')==1)
            % check right parameters
    % Implant Type - Important
    if (isfield(mod_map.Right,'ImplantType')==0)
        %mod_params.ImplantType = std_params.ImplantType;
        mod_map.Right.ImplantType = std_params.ImplantType;
        errordlg('Right: Implant Type is unknown!', 'Missing MAP info');
        disp(['Right: Missing Info - Implant Type - Default value of: ', std_params.ImplantType, ' loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    
    %Sampling Frequency
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
    
    %Strategy
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
    
    %StimulationMode
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
    
    %ChannelOrderType
    if (isfield(mod_map.Right,'ChannelOrderType')==0)
        mod_map.Right.ChannelOrderType = std_params.ChannelOrderType;
        disp([ 'Right: Missing Info - ChannelOrderType - Default order: ', std_params.ChannelOrderType, ' loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    
    %FrequencyTable
    if (isfield(mod_map.Right,'FrequencyTable')==0)
        mod_map.Right.FrequencyTable = std_params.FrequencyTable;
        disp([ 'Right: Missing Info - FrequencyTable: ', std_params.FrequencyTable, '  Frequency Table loaded']);
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    
    %Window
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
    
    mod_map.Right.NumberOfBands          = size(mod_map.Right.El_CF1_CF2_THR_MCL_Gain, 1);    % Number of active electrodes/bands
    mod_map.Right.Electrodes             = mod_map.Right.El_CF1_CF2_THR_MCL_Gain(:, 1);       % Active Electrodes
    mod_map.Right.LowerCutOffFrequencies = mod_map.Right.El_CF1_CF2_THR_MCL_Gain(:, 2);       % Low cut-off frequencies of filters
    mod_map.Right.UpperCutOffFrequencies = mod_map.Right.El_CF1_CF2_THR_MCL_Gain(:, 3);       % Upper cut-off frequencies of filters
    mod_map.Right.THR                    = mod_map.Right.El_CF1_CF2_THR_MCL_Gain(:, 4);       % Threshold Levels (THR)
    mod_map.Right.MCL                    = mod_map.Right.El_CF1_CF2_THR_MCL_Gain(:, 5);       % Maximum Comfort Levels (MCL)
    mod_map.Right.BandGains              = mod_map.Right.El_CF1_CF2_THR_MCL_Gain(:, 6);       % Individual Band Gains (dB)
    
    %Comments
    if (isfield(mod_map.Right,'Comments')==0)
        mod_map.Right.Comments = std_params.Comments;
        right_flag = 1; % Flag is set to 1 if any default value is used
    end
    
    if (right_flag==1)
        comments = [mod_map.Right.Comments ' | RIGHT: One or more missing values were found in the MAP, which were loaded by default values'];
        mod_map.Right.Comments = comments;
    end
    
    end
    
    
    
end




