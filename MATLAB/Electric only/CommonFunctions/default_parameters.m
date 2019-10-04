% Default Processing/Stimulation Parameters
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright: CRSS-CILab, UT-Dallas
%   Authors: Hussnain Ali
%      Date: 2015/09/28
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [params] = default_parameters

params.ImplantType        = 'CI24RE';     %Implant chip type, e.g., CI24RE(CS/CA), CI24R, CI24M, CI22M, ST
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
params.NumberOfBands          = size(params.El_CF1_CF2_THR_MCL_Gain, 1);    % Number of active electrodes/bands
params.Electrodes             = params.El_CF1_CF2_THR_MCL_Gain(:, 1);       % Active Electrodes
params.LowerCutOffFrequencies = params.El_CF1_CF2_THR_MCL_Gain(:, 2);       % Low cut-off frequencies of filters
params.UpperCutOffFrequencies = params.El_CF1_CF2_THR_MCL_Gain(:, 3);       % Upper cut-off frequencies of filters
params.THR                    = params.El_CF1_CF2_THR_MCL_Gain(:, 4);       % Threshold Levels (THR)
params.MCL                    = params.El_CF1_CF2_THR_MCL_Gain(:, 5);       % Maximum Comfort Levels (MCL)
params.BandGains              = params.El_CF1_CF2_THR_MCL_Gain(:, 6);       % Individual Band Gains (dB)
params.Comments               = '';                                           % Optional: comments
