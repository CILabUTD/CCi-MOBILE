% MAP file structure for CCI Platform

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright: CRSS-CILab, UT-Dallas
%   Authors: Hussnain Ali
%      Date: 2015/09/28
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

MAP.General.SubjectName      = 'Subject 01';       % Optional: Subject Name
MAP.General.SubjectID        = 'S01';              % Optional: Random Subject ID
MAP.General.MapTitle         = 'S01_ACE_900Hz';    % Optional: Map Title

MAP.General.NumberOfImplants = 2;                  % '1' for Unilateral and '2' for Bilateral
MAP.General.ImplantedEar     = 'Bilateral';        % 'Left' for left side; 'Right' for right side; 'Bilateral' for both sides
MAP.General.StimulateEars    = 'Bilateral';        % 'Left' for left only; 'Right' for right only; 'Both'|'Bilateral' for both sides; 'NULL' for no stimulation

%% Left Ear Parameters
%  remove this section if left side does not exist
MAP.Left.ImplantType        = 'CI24RE';     %Implant chip type, e.g., CI24RE(CS/CA), CI24R, CI24M, CI22M, ST
MAP.Left.SamplingFrequency  = 16000;        % Fixed
MAP.Left.NumberOfChannels   = 22;           % 22 fixed for imlants from Cochlear Ltd.
MAP.Left.Strategy           = 'ACE';        % 'ACE' or 'CIS' or 'Custom'
MAP.Left.Nmaxima            = 8;            % Nmaxima 1 - 22 for n-of-m strategies
MAP.Left.StimulationMode    = 'MP1+2';      % Electrode Configuration/Stimulation mode e.g., MP1, MP1+2, BP1, BP1+2, CG,....etc.
MAP.Left.StimulationRate    = 1000;         % Stimulation rate per electrode in number of pulses per second (pps)
MAP.Left.PulseWidth         = 25;           % Pulse width in us
MAP.Left.IPG                = 8;            % Inter-Phase Gap (IPG) fixed at 8us (could be variable in future)
MAP.Left.Sensitivity        = 2.3;          % Microphone Sensitivity (adjustable in GUI)
MAP.Left.Gain               = 25;           % Global gain for envelopes in dB - standard is 25dB (adjustable in GUI)
MAP.Left.Volume             = 10;           % Volume Level on a scale of 0 to 10; 0 being lowest and 10 being highest (adjustable in GUI)
MAP.Left.Q                  = 20;           % Q-factor for the compression function
MAP.Left.BaseLevel          = 0.0156;       % Base Level
MAP.Left.SaturationLevel    = 0.5859;       % Saturation Level
MAP.Left.ChannelOrderType   = 'base-to-apex'; % Channel Stimulation Order type: 'base-to-apex' or 'apex-to-base'
MAP.Left.FrequencyTable     = 'Default';    % Frequency assignment for each band "Default" or "Custom"
MAP.Left.Window             = 'Hanning'     % Window type
MAP.Left.El_CF1_CF2_THR_MCL_Gain = [
  % El  F_Low   F_High  THR     MCL     Gain
    22  188     313     100     200     0.0
    21	313     438     100     200     0.0
    20	438     563     100     200     0.0
    19	563     688     100     200     0.0
    18	688     813     100     200     0.0
    17	813     938     100     200     0.0
    16	938     1063    100     200     0.0
    15	1063    1188    100     200     0.0
    14	1188    1313    100     200     0.0
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
MAP.Left.NumberOfBands          = size(MAP.Left.El_CF1_CF2_THR_MCL_Gain, 1);    % Number of active electrodes/bands
MAP.Left.Electrodes             = MAP.Left.El_CF1_CF2_THR_MCL_Gain(:, 1);       % Active Electrodes
MAP.Left.LowerCutOffFrequencies = MAP.Left.El_CF1_CF2_THR_MCL_Gain(:, 2);       % Low cut-off frequencies of filters
MAP.Left.UpperCutOffFrequencies = MAP.Left.El_CF1_CF2_THR_MCL_Gain(:, 3);       % Upper cut-off frequencies of filters
MAP.Left.THR                    = MAP.Left.El_CF1_CF2_THR_MCL_Gain(:, 4);       % Threshold Levels (THR)
MAP.Left.MCL                    = MAP.Left.El_CF1_CF2_THR_MCL_Gain(:, 5);       % Maximum Comfort Levels (MCL)
MAP.Left.BandGains              = MAP.Left.El_CF1_CF2_THR_MCL_Gain(:, 6);       % Individual Band Gains (dB)
MAP.Left.Comments               = '';                                           % Optional: comments

%% Right Ear Parameters
%  remove this section if right side does not exist
MAP.Right.ImplantType       = 'CI24RE';
MAP.Right.SamplingFrequency = 16000;            % Fixed
MAP.Right.NumberOfChannels  = 22;               % 22 fixed for imlants from Cochlear Ltd.
MAP.Right.Strategy          = 'ACE';            % 'ACE' or 'CIS' or 'Custom'
MAP.Right.Nmaxima           = 8;                % Nmaxima 1 - 22 for n-of-m strategies
MAP.Right.StimulationMode   = 'MP1+2';          % Electrode Configuration/Stimulation mode e.g., MP1, MP1+2, BP1, BP1+2, CG,....etc.
MAP.Right.StimulationRate   = 1000;             % Stimulation rate per electrode in number of pulses per second (pps)
MAP.Right.PulseWidth        = 25;               % Pulse width in us
MAP.Right.IPG               = 8;                % Inter-Phase Gap (IPG) fixed at 8us (could be variable in future)
MAP.Right.Sensitivity       = 2.3;              % Microphone Sensitivity (adjustable in GUI)
MAP.Right.Gain              = 25;               % Global gain for envelopes in dB - standard is 25dB (adjustable in GUI)
MAP.Right.Volume            = 10;               % Volume Level on a scale of 0 to 10; 0 being lowest and 10 being highest (adjustable in GUI)
MAP.Right.Q                 = 20;               % Q-factor for the compression function
MAP.Right.BaseLevel         = 0.0156;           % Base Level
MAP.Right.SaturationLevel   = 0.556;            % Saturation Level
MAP.Right.ChannelOrderType  = 'base-to-apex';   % Channel Stimulation Order type: 'base-to-apex' or 'apex-to-base'
MAP.Right.FrequencyTable     = 'Default';        % Frequency assignment for each band "Default" or "Custom"
MAP.Right.Window            = 'Hanning'         % Window type
MAP.Right.El_CF1_CF2_THR_MCL_Gain = [
  % El  F_Low   F_High  THR     MCL     Gain
    22  188     313     100     200     0.0
    21	313     438     100     200     0.0
    20	438     563     100     200     0.0
    19	563     688     100     200     0.0
    18	688     813     100     200     0.0
    17	813     938     100     200     0.0
    16	938     1063    100     200     0.0
    15	1063    1188    100     200     0.0
    14	1188    1313    100     200     0.0
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
MAP.Right.NumberOfBands             = size(MAP.Right.El_CF1_CF2_THR_MCL_Gain, 1);   % Number of active electrodes/bands
MAP.Right.Electrodes                = MAP.Right.El_CF1_CF2_THR_MCL_Gain(:, 1);      % Active Electrodes
MAP.Right.LowerCutOffFrequencies    = MAP.Right.El_CF1_CF2_THR_MCL_Gain(:, 2);      % Low cut-off frequencies of filters
MAP.Right.UpperCutOffFrequencies    = MAP.Right.El_CF1_CF2_THR_MCL_Gain(:, 3);      % Upper cut-off frequencies of filters
MAP.Right.THR                       = MAP.Right.El_CF1_CF2_THR_MCL_Gain(:, 4);      % Threshold Levels (THR)
MAP.Right.MCL                       = MAP.Right.El_CF1_CF2_THR_MCL_Gain(:, 5);      % Maximum Comfort Levels (MCL)
MAP.Right.BandGains                 = MAP.Right.El_CF1_CF2_THR_MCL_Gain(:, 6);      % Individual Band Gains (dB)
MAP.Right.Comments                  = '';                                           % Optional: comments