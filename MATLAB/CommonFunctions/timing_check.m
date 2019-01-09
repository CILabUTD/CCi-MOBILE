% Error Checking Routine
% Author: Hussnain Ali
% Date:   12/07/2010
%         University of Texas at Dallas (c) Copyright 2010
%
% Parameter Checking routines for rate and pulse width adjustments
% Used to calculate correct number of pulse per frame per channel

function [mod_map] = timing_check(org_map)

mod_map=org_map;

if (isfield(org_map,'Left')==1)
    % check left parameters
    num_selected=org_map.Left.Nmaxima;
    pw=org_map.Left.PulseWidth;
    rate=org_map.Left.StimulationRate;
    ipg = org_map.Left.IPG;
    [rate_outL, pw_outL, ppfpchL] = check_timing_parameters(num_selected, rate, pw, ipg);
    mod_map.Left.StimulationRate=rate_outL;
    mod_map.Left.PulseWidth=pw_outL;
    mod_map.Left.pulses_per_frame_per_channel=ppfpchL;
    mod_map.Left.pulses_per_frame=ppfpchL*num_selected;
end

if (isfield(org_map,'Right')==1)
    % check right parameters
    num_selected=org_map.Right.Nmaxima;
    pw=org_map.Right.PulseWidth;
    rate=org_map.Right.StimulationRate;
    ipg = org_map.Right.IPG;
    [rate_outR, pw_outR, ppfpchR] = check_timing_parameters(num_selected, rate, pw, ipg);
    mod_map.Right.StimulationRate=rate_outR;
    mod_map.Right.PulseWidth=pw_outR;
    mod_map.Right.pulses_per_frame_per_channel=ppfpchR;
    mod_map.Right.pulses_per_frame=ppfpchR*num_selected;
end


%% Check timing
    function [rate_set, pw_set, pulses_per_frame_per_channel] = check_timing_parameters(num_selected, rate, pw, IPG)
        pw_org = pw; rate_org = rate;
        % Check Pulse Width
        if pw>400
            pw=400;
        end
        if pw<25
            pw=25;
        end
        
        SG=0; AG=1;
        Total_Stimulation_Rate=14400; thr=Total_Stimulation_Rate; % This is the maximum stimulation rate supported by CIC4 in STD mode
        
        if (num_selected*rate<=thr)&&(rate<=thr)
            protocol='STD'; %%% Standard Rate Protocol
            max_pw_STD=floor(1e6/(rate*num_selected) - (IPG+SG+AG));
            if pw>max_pw_STD
                pw=max_pw_STD;
            else
                % Keep the same pw
            end
        else
            errordlg('High Rate Protocol is currently not supported');
            protocol='HRT'; % High Rate Protocol (HR)
            while (num_selected*rate>thr)
                rate=rate-1;
            end
            rate=floor(rate);
            % Inform user abt the rate changed
        end
        
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        %% Pulse Width Centric
        %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        
        rate = floor(rate);
        pw = floor(pw);
        pulses_per_frame_per_channel=((8.0*rate)/1000); %floor(0.5+floor((8.0*rate_set)/1000));
        pulses_per_frame= floor(num_selected*pulses_per_frame_per_channel);
                
        minimum_possible_rate = 125; % for at least one pulse per frame per channel
        minimum_possible_pulses_per_frame_per_channel = 1;
        minimum_possible_pulses_per_frame = num_selected;
        
        %max_pw = floor(0.5*((8000/num_selected) - (IPG+6.0+1.0)));
        max_pw = floor(0.5*((1e6/(rate*num_selected)) - (11+IPG))); % for MP stimulation modes in CIC3/CIC4 
        %max_pw = floor(0.5*((1e6/(rate*num_selected)) - (11+IPG+200))); % for BP and CG stimulation modes in CIC3/CIC4 
        
        if pw>max_pw
            pw=max_pw;
        else
            % Keep the same pw
        end
        
        
        pd1 = 8000/pulses_per_frame; pd2 = pw*2.0+IPG+6.0+1.0;
        if (pd1<pd2)
            ratetest = rate;
            while (pd1<pd2)
                ratetest = ratetest-1;
                pulses_per_frame_per_channel=((8.0*rate)/1000); %floor(0.5+floor((8.0*rate_set)/1000));
                pulses_per_frame= floor(num_selected*pulses_per_frame_per_channel);
                pd1 = 8000/pulses_per_frame;
            end
            rate = floor(ratetest);
        end
        
        pulses_per_frame_per_channel=((8.0*rate)/1000); %floor(0.5+floor((8.0*rate_set)/1000));
        pulses_per_frame= floor(num_selected*pulses_per_frame_per_channel);
        rate_set=rate; 
        pw_set = pw;
        
        if rate_set~=rate_org && pw_set~=pw_org
            %warndlg('Rate and Pulse Width has been adjusted','Rate and Pulse-width adjustment')
            disp(['Rate has been adjusted to: ', num2str(rate_set), ' pps']);
            disp(['Pulse Width has been adjusted to: ', num2str(pw_set), ' us']);
        elseif pw_set~=pw_org
            %warndlg('Pulse Width has been adjusted','Pulse-width adjustment')
            disp(['Pulse Width has been adjusted to: ', num2str(pw_set), ' us']);
        elseif rate_set~=rate_org
            %warndlg('Rate has been adjusted','Rate adjustment')
            disp(['Rate has been adjusted to: ', num2str(rate_set), ' pps']);
        end
        
    end


end