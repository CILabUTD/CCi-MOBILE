function u = plot_electrodogram(varargin)

% Plot_sequence: Plot a cell array of sequences.
% Plots a sequence as one vertical line segment per pulse, 
% with height proportional to magnitude.
% If a cell array of sequences is given, they are displayed one at a time.
%
% User interface:
%
% Zoom is controlled by mouse click and drag.
%
% Key presses:
% numeric keys '1'-'9': display the n'th sequence.
% '0':                  display last sequence
% '['                   display previous sequence
% ']'                   display next sequence
%
% u = Plot_sequence(seq, title_str, channels)
%
% seq:       A sequence or cell array of sequences
% title_str: A string or cell array of strings, used as the window title(s).
% channels:  A vector containing the lowest & highest channel numbers to be displayed.
%              Defaults to the min and max channel numbers present in the sequence(s).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%    Copyright: Cochlear Ltd
%     $Archive: /Nucleus_MATLAB_Toolbox/NMT/Sequence/Plot_sequence.m $
%    $Revision: 17 $
%        $Date: 31/03/05 2:36p $
%      Authors: Brett Swanson
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

if ischar(varargin{1})
	u = feval(varargin{:});		% Callbacks
else
	u = Init(varargin{:});
end
end
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%
%%*******************************************************************

function u = Init(seq, title_str, channels)

	if iscell(seq)		
		u.seqs = seq;
	else
		u.seqs = {seq};
	end
	u.num_seqs = length(u.seqs);

	if nargin < 2
		title_str = 'Sequence';
	end
	u.title_str = Init_title_strings(title_str, u.num_seqs);
	
	is_channels = 1;
	for n = 1:u.num_seqs	
		if ~isfield(u.seqs{n}, 'channels')
			u.seqs{n}.channels = 23 - u.seqs{n}.electrodes;
			is_channels = 0;
		end;
		if ~isfield(u.seqs{n}, 'magnitudes')
			u.seqs{n}.magnitudes = u.seqs{n}.current_levels;
		else
			idles = (u.seqs{n}.magnitudes < 0);
			if any(idles)
				if length(u.seqs{n}.channels) == 1	% replicate constant channel
					u.seqs{n}.channels = repmat(u.seqs{n}.channels, length(u.seqs{n}.magnitudes), 1);
				end;
				u.seqs{n}.channels(idles)   = 0;
				u.seqs{n}.magnitudes(idles) = 0;
			end;
		end;
		min_channels(n) = min(u.seqs{n}.channels);
		max_channels(n) = max(u.seqs{n}.channels);
		max_mags(n)     = max(u.seqs{n}.magnitudes);
		%max_times(n)	= Get_sequence_duration(u.seqs{n});
        seq = u.seqs{n};
        num_pulses = Get_num_pulses(seq);

        if length(seq.periods) == 1
            max_times(n) = seq.periods * num_pulses;
        else
            max_times(n) = sum(seq.periods);
        end;

		min_times(n)	= u.seqs{n}.periods(1);
	end;
	
	if exist('channels')
		u.min_channel = min(channels);
		u.max_channel = max(channels);	
	else
		u.min_channel = min(min_channels);
		u.max_channel = max(max_channels);
	end;
	u.max_mag     = max(max_mags);
	u.max_time    = max(max_times);
	u.min_time    = -0.25 * max(min_times);

	if (u.max_time > 5000)
		time_scale = 1000;
		time_label = 'Time (ms)';
	else
		time_scale = 1;
		time_label = 'Time (us)';
	end;

	u.h_figure = figure('Visible', 'off');
    s = [mfilename, '(''', 'KeyPress', ''');'];
	set(u.h_figure, 'KeyPressFcn', s);
	u.h_axes   = axes;
	
	yticks = u.min_channel:u.max_channel;
	set(gca, 'YTick', yticks);
	set(gca, 'TickDir', 'out');
	ylabel('Channel');
	if ~is_channels
		set(gca,'YTickLabel', 23 - yticks);
		ylabel('Electrode');
	end
	for n = 1:u.num_seqs
		u.h_lines(n) = Plot_sequence_as_lines(u.seqs{n}, u.max_mag/0.75, time_scale);
		set(u.h_lines(n), 'Visible', 'off');
	end;
	
	axis([u.min_time/time_scale, u.max_time/time_scale, u.min_channel-1, u.max_channel+1]);
	zoom on;
	
	u.cell_index = 1;
	%Set_figure_data(u);
    fig_handle = get(0,'CurrentFigure');

    %fig_handle = Get_figure;
    set(fig_handle, 'UserData', u);

	Set_cell_index(u, 1);
	
	xlabel(time_label);
	set(u.h_figure, 'Visible', 'on');
end

%% 
%%********************************************************************
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [num_pulses, field_lengths] = Get_num_pulses(seq)
% Get_num_pulses: Returns the number of pulses in a sequence.
% function [num_pulses, field_lengths] = Get_num_pulses(seq)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%    Copyright: Cochlear Ltd
%     $Archive: /Matlab/Sequence/Get_num_pulses.m $
%    $Revision: 1 $
%        $Date: 2/10/01 11:44a $
%      Authors: Brett Swanson
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

field_names   = fieldnames(seq);
num_fields    = length(field_names);
field_lengths = zeros(num_fields, 1);

for n = 1:num_fields
	name = field_names{n};
	vec  = getfield(seq, name);
	field_lengths(n) = length(vec);
end;

% Check that all fields have length equal to 1 or N:

num_pulses = max(field_lengths);	% N
if num_pulses > 1
	implied_field_lengths = field_lengths;
	% Vectors with length 1 imply that all N pulses have that value:
	implied_field_lengths(field_lengths == 1) = num_pulses;
	short_field_indices = find(implied_field_lengths < num_pulses);
	if (~isempty(short_field_indices))
		disp('Some fields were too short:');
		disp(char(field_names{short_field_indices}));
		error(' ');
	end;
end;
end
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% A convenience function to save awkward quoting when setting up the callbacks:

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Plot sequence as one vertical line segment per pulse, 
% with height proportional to magnitude.
% The scaling factor max_mag is passed in so that multiple sequences
% can all be drawn with the same scale.
% The entire sequence is plotted as one "line" handle.
% This is much faster than a separate handle for each pulse.
% NaNs are used to separate the line segments for each pulse.
function title_str = Init_title_strings(title_str, num_titles)

% Init_title_strings: Initialise a cell array of title strings.
% Used in Plot_sequence, Plot_waveforms.
%
% title_str = Init_title_strings(title_str, num_titles)
%
% title_str:   A string or cell array of strings, used as window title(s).
% num_titles:  The expected number of strings.

if ischar(title_str)
    title_str = {title_str};
end

if num_titles > 1
    if length(title_str) == 1
        title_str = repmat(title_str, 1, num_titles);
    elseif length(title_str) < num_titles
        error('Insufficient number of title strings');
    end

    for n = 1:num_titles
        title_str{n} = [title_str{n}, ' (', num2str(n), ')'];
    end
end
end
function hdl = Plot_sequence_as_lines(seq, max_mag, time_scale)

%	t = Get_pulse_times(seq);				% column_vector
    
num_pulses = Get_num_pulses(seq);

if length(seq.periods) == 1
	t = seq.periods * (0:num_pulses)';
else
	t = [0; cumsum(seq.periods)];
end;

%duration = t(end);
t(end) = [];

	t = t' / time_scale;					% row vector
	z = repmat(NaN, size(t));
	
	x = [t; t; z];
	x = x(:);								% column vector

	c = seq.channels';						% Bottom of line aligns with channel Y axis tick.
	if length(c) == 1
		c = repmat(c, size(t));
	end;
	m = seq.magnitudes';
	h = c + m / max_mag;					% Line height is proportional to magnitude.
	y = [c; h; z];
	y = y(:);								% column vector

	hdl = line(x, y, 'Color', 'black');
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% set cell index
function Set_cell_index(u, cell_index)

	set(u.h_lines(u.cell_index), 'Visible', 'off');

	if (cell_index < 1)
		u.cell_index = u.num_seqs;
	elseif (cell_index > u.num_seqs)
		u.cell_index = 1;
	else
		u.cell_index = cell_index;
	end;
	
	set(u.h_lines(u.cell_index), 'Visible', 'on');
	%Window_title(u.title_str{u.cell_index});
    set(gcf, 'numbertitle', 'off');
    set(gcf, 'name', u.title_str{u.cell_index});


	set(u.h_figure, 'UserData', u);
end
