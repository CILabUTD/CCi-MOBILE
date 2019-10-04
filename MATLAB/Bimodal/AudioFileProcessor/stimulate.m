function stimulate( filename , map )
%close all;
% stimulus.magnitudes.right = stimulus.magnitudes.left; % copy left to right
% stimulus.magnitudes.right(1:end) = 0;
% stimulus.electrodes.right=stimulus.electrodes.left;% copy left to right

%%  Call ACE processing routine
if (isfield(map,'Left') ==1)
    %sine_token_l=uint8(150.*sin(2.*pi.*(0:1:p.Left.pulses_per_frame).*0.01)); %figure; plot(sine_token);
    map.Left.lr_select = 'left'; %%% left - - - Process the left implant first
    audio_signal  = AudioSignal( filename, map.Left.lr_select );
    stimuli.left = ACE_Process(audio_signal, map.Left);
    stimuli.left.audio = (1/map.Left.scale_factor).*audio_signal;
end

if (isfield(map,'Right') ==1)
    %sine_token_l=uint8(150.*sin(2.*pi.*(0:1:p.Left.pulses_per_frame).*0.01)); %figure; plot(sine_token);
    map.Right.lr_select = 'right'; %%% right - - - Process the right implant first
    audio_signal  = AudioSignal( filename, map.Right.lr_select );
    stimuli.right = ACE_Process(audio_signal,map.Right);
    stimuli.right.audio = (1/map.Right.scale_factor).*audio_signal;
end
stimuli.map=map;

% (Optional) Save stimulus structure for later streaming
% save ('stimulus_file', '-struct', 'stimuli');

%% Stream the stimulus to the CCI-Mobile Platform
Stream (stimuli);

return