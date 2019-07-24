function [app] = checkOperatingLimits(app,input,adjusted,on,verbose,priority,ear)
% This function is used to check if user-specific parameters are within the
% operating ranges of the CCi-MOBILE research platform. 

%  INPUT PARAMETERS
%   input    = original MAP 
%   adjusted = adjusted MAP
%   on       = On/Off switch for realtime(0)/offline processing(1)
%   warning  = On/Off switch to display dialog box upon initialization
%   priority = User-specified parameters calls priority algorithm
%   ear      = 'R' or 'L'

fs   = input.SamplingFrequency;
flag = 0;
if priority == 0
if on == 1
   % Check the individual channel stimulation rate against the actual analysis rate
        rate_org = (fs/(fs/input.StimulationRate));
        %rate_org = round(fs/(fs/input.StimulationRate));
        if adjusted.analysis_rate~=(rate_org)
         line_one = sprintf(['Stimulation Rate Specified: ' num2str(rate_org) ' pps\nSTATUS: Outside Operating Specifications - ADJUSTED\nStimulation Rate Adjusted:  ' num2str(adjusted.analysis_rate) ' pps\n']);
         flag = 1;
            % Update the app parameters 
            if strcmp(ear,'R')
                % Adjust the right ear parameters
                app.SRmapRval.Value      = num2str(rate_org);
                app.SRstatusRval.Value   = 'Outside Operating Specifications';
                app.SRadjustedRval.Value = num2str(adjusted.analysis_rate);
            elseif strcmp(ear,'L')
                % Adjust the left ear parameters
                app.SRmapLval.Value      = num2str(rate_org);
                app.SRstatusLval.Value   = 'Outside Operating Specifications';
                app.SRadjustedLval.Value = num2str(adjusted.analysis_rate);
            end
        else
         % Dialog box output 
         line_one = sprintf(['Stimulation Rate Specified: ' num2str(rate_org) ' pps\nSTATUS: Within Operating Specifications \n']);
            % Update the app parameters 
            if strcmp(ear,'R')
                % Adjust the right ear parameters
                app.SRmapRval.Value      = num2str(rate_org);
                app.SRstatusRval.Value    = 'Within Operating Specs';
                app.SRadjustedRval.Value  = '';
            elseif strcmp(ear,'L')
                % Adjust the left ear parameters
                app.SRmapLval.Value      = num2str(rate_org);
                app.SRstatusLval.Value   = 'Within Operating Specs';
                app.SRadjustedLval.Value = '';
            end
        end
        clear rate_org
   % Check the TOTAL stimulation rate
        tsr_org = input.StimulationRate*input.Nmaxima;
        if adjusted.total_rate~=(tsr_org)
         % Dialog box output for: Total pulses per stimulation cycle
         line_two = sprintf(['Total Pulses Per Stimulation Cycle Specified: ' num2str(tsr_org) ' pulses\nSTATUS: Outside Operating Specifications - ADJUSTED\nTotal Pulses Per Stimulation Cycle Adjusted:  ' num2str(adjusted.total_rate) ' pulses\n']);
         flag = 1;
            % Update the app parameters 
            if strcmp(ear,'R')
                % Adjust the right ear parameters
                app.TPPSmapRval.Value      = num2str(tsr_org);
                app.TPPSstatusRval.Value    = 'Outside Operating Specs';
                app.TPPSadjustedRval.Value = num2str(adjusted.total_rate);
            elseif strcmp(ear,'L')
                % Adjust the left ear parameters
                app.TPPSmapLval.Value      = num2str(tsr_org);
                app.TPPSstatusLval.Value    = 'Outside Operating Specs';
                app.TPPSadjustedLval.Value = num2str(adjusted.total_rate);
            end
        else
         % Update the dialog box
         line_two = sprintf(['Total Pulses Per Stimulation Cycle Specified: ' num2str(tsr_org) '\nSTATUS: Within Operating Specifications \n']);
            % Update the app parameters 
            if strcmp(ear,'R')
                % Adjust the right ear parameters
                app.TPPSmapRval.Value      = num2str(tsr_org);
                app.TPPSstatusRval.Value    = 'Within Operating Specs';
                app.TPPSadjustedRval.Value  = '';
            elseif strcmp(ear,'L')
                % Adjust the left ear parameters
                app.TPPSmapLval.Value      = num2str(tsr_org);
                app.TPPSstatusLval.Value    = 'Within Operating Specs';
                app.TPPSadjustedLval.Value  = '';
            end
        end
        clear tsr_org
        
   % Check the new pulses per frame per channel
        ppfpch_org = (8.0/1000*input.StimulationRate);
        %ppfpch_org = ceil(8.0/1000*input.StimulationRate);
        if adjusted.pulses_per_frame_per_channel~=(ppfpch_org)
         % Update the dialog box
         line_three = sprintf(['Pulses Per Frame Per Channel Specified: ' num2str(ppfpch_org) '\nSTATUS: Outside Operating Specifications - ADJUSTED\nPulses Per Frame Per Channel Adjusted:  ' num2str(adjusted.pulses_per_frame_per_channel) ' pulses\n']);
         flag = 1;
            % Update the app parameters 
            if strcmp(ear,'R')
                % Adjust the right ear parameters
                app.PPFPCmapRval.Value      = num2str(ppfpch_org);
                app.PPFPCstatusRval.Value    = 'Outside Operating Specs';
                app.PPFPCadjustedRval.Value = num2str(adjusted.pulses_per_frame_per_channel);
            elseif strcmp(ear,'L')
                % Adjust the left ear parameters
                app.PPFPCmapLval.Value      = num2str(ppfpch_org);
                app.PPFPCstatusLval.Value    = 'Outside Operating Specs';
                app.PPFPCadjustedLval.Value = num2str(adjusted.pulses_per_frame_per_channel);
            end
        else
         % Update the dialog box
         line_three = sprintf(['Pulses Per Frame Per Channel Specified: ' num2str(ppfpch_org) '\nSTATUS: Within Operating Specifications \n']);
            % Update the app parameters 
            if strcmp(ear,'R')
                % Adjust the right ear parameters
                app.PPFPCmapRval.Value      = num2str(ppfpch_org);
                app.PPFPCstatusRval.Value    = 'Within Operating Specs';
                app.PPFPCadjustedRval.Value  = '';
            elseif strcmp(ear,'L')
                % Adjust the left ear parameters
                app.PPFPCmapLval.Value      = num2str(ppfpch_org);
                app.PPFPCstatusLval.Value    = 'Within Operating Specs';
                app.PPFPCadjustedLval.Value  = '';
            end
        end
        clear ppfpch_org
        
   % Check the new pulses per frame 
        ppfNEWsr_org = adjusted.Nmaxima*(8.0/1000*input.StimulationRate);
        if adjusted.pulses_per_frame~=(ppfNEWsr_org)
         line_four = sprintf(['Pulses-Per-Frame Specified: ' num2str(ppfNEWsr_org) ' pulses\nSTATUS: Outside Operating Specifications - ADJUSTED\nPulses-Per-Frame Adjusted:  ' num2str(adjusted.pulses_per_frame) ' pulses\n']);
         flag = 1;
        else
         line_four = sprintf(['Pulses-Per-Frame Specified: ' num2str(ppfNEWsr_org) ' pulses\nSTATUS: Within Operating Specifications \n']);
        end
        clear ppfNEWsr_org
        
   % Check the pulse width
        if adjusted.PulseWidth~=input.PulseWidth
         % Update the dialog box
         line_six = sprintf(['Pulse Width Specified: ' num2str(input.PulseWidth) ' us\nSTATUS: Outside Operating Specifications - ADJUSTED\nPulse Width Adjusted:  ' num2str(adjusted.PulseWidth) ' us\n']);
         flag = 1;
            % Update the app parameters 
            if strcmp(ear,'R')
                % Adjust the right ear parameters
                app.PWmapRval.Value      = num2str(input.PulseWidth);
                app.PWstatusRval.Value    = 'Outside Operating Specs';
                app.PWadjustedRval.Value = num2str(adjusted.PulseWidth);
            elseif strcmp(ear,'L')
                % Adjust the left ear parameters
                app.PWmapLval.Value      = num2str(input.PulseWidth);
                app.PWstatusLval.Value    = 'Outside Operating Specs';
                app.PWadjustedLval.Value = num2str(adjusted.PulseWidth);
            end
        else
         % Update the dialog box
         line_six = sprintf(['Pulse Width Specified: ' num2str(input.PulseWidth) ' us\nSTATUS: Within Operating Specifications \n']);
            % Update the app parameters 
            if strcmp(ear,'R')
                % Adjust the right ear parameters
                app.PWmapRval.Value       = num2str(input.PulseWidth);
                app.PWstatusRval.Value    = 'Within Operating Specs';
                app.PWadjustedRval.Value  = '';
            elseif strcmp(ear,'L')
                % Adjust the left ear parameters
                app.PWmapLval.Value      = num2str(input.PulseWidth);
                app.PWstatusLval.Value    = 'Within Operating Specs';
                app.PWadjustedLval.Value  = '';
            end
        end
        
   % Check the block shift 
        block_shift_org = fs/input.StimulationRate;
        if adjusted.block_shift~=block_shift_org
            % Update the dialog box
         line_five = sprintf(['Block Shift Specified: ' num2str(block_shift_org) ' samples\nSTATUS: Outside Operating Specifications - ADJUSTED\nBlock Shift Adjusted: ' num2str(adjusted.block_shift) ' samples\n']);
         flag = 1;
            % Update the app parameters 
            if strcmp(ear,'R')
                % Adjust the right ear parameters
                app.BSmapRval.Value      = num2str(block_shift_org);
                app.BSstatusRval.Value    = 'Outside Operating Specs';
                app.BSadjustedRval.Value = num2str(adjusted.block_shift);
            elseif strcmp(ear,'L')
                % Adjust the left ear parameters
                app.BSmapLval.Value      = num2str(block_shift_org);
                app.BSstatusLval.Value    = 'Outside Operating Specs';
                app.BSadjustedLval.Value = num2str(adjusted.block_shift);
            end
        else
         % Update the dialog box
         line_five = sprintf(['Block Shift Specified: ' num2str(block_shift_org) ' samples\nSTATUS: Within Operating Specifications \n']); 
            % Update the app parameters 
            if strcmp(ear,'R')
                % Adjust the right ear parameters
                app.BSmapRval.Value      = num2str(block_shift_org);
                app.BSstatusRval.Value    = 'Within Operating Specs';
                app.BSadjustedRval.Value  = '';
            elseif strcmp(ear,'L')
                % Adjust the left ear parameters
                app.BSmapLval.Value      = num2str(block_shift_org);
                app.BSstatusLval.Value    = 'Within Operating Specs';
                app.BSadjustedLval.Value  = '';
            end
        end
   % DISPLAY WARNING NOTIFICATION DLG BOX
   if verbose == 1
    if strcmp(ear,'R')
        ear = 'RIGHT';
    else
        ear = 'LEFT';
    end
    switch flag
     case 0
      % All parameters were accepted - Within Operating Range
      notify = msgbox(sprintf(['STATUS OF USER-SPECIFIED ' ear ' EAR PARAMETERS: \n\n' line_one '\n' line_two '\n' line_three '\n' line_four '\n' line_five '\n' line_six]),'User Specified Parameters Within Operating Ranges');
     case 1
      % One of the parameters was adjusted - Outside Operating Range
      notify = warndlg(sprintf(['STATUS OF USER-SPECIFIED ' ear ' EAR PARAMETERS: \n\n' line_one '\n' line_two '\n' line_three '\n' line_four '\n' line_five '\n' line_six]),'Warning! - Adjusted User Specified Parameters');
    end
   end % No warning dialog box selected - 'off'
  
end % checkOperatingLimits is TURNED OFF
end % PRIORITY PARAMETERS HAVE ALREADY BEEN SET
end