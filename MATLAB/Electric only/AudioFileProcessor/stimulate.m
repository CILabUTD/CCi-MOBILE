function stimulate( filename , map )

%%  Call ACE processing routine
if (isfield(map,'Left') ==1)
    map.Left.lr_select = 'left'; %%% left - - - Process the left implant first
    audio_signal  = AudioSignal( filename, map.Left.lr_select );
    stimuli.left = ACE_Process(audio_signal, map.Left);
end

if (isfield(map,'Right') ==1)
    map.Right.lr_select = 'right'; %%% right - - - Process the right implant first
    audio_signal  = AudioSignal( filename, map.Right.lr_select );
    stimuli.right = ACE_Process(audio_signal,map.Right);
end
stimuli.map=map;

% (Optional) Save stimulus structure for later streaming
% [PATHSTR,NAME,EXT] = fileparts(filename);
% save (['stimuli_CCiMOBILE_', NAME], '-struct', 'stimuli');
% saveas(gcf, ['Electrodogram_CCiMOBILE_', NAME], 'fig'); % Save figure
%% Stream the stimulus to the CCI-Mobile Platform
Stream (stimuli);

return