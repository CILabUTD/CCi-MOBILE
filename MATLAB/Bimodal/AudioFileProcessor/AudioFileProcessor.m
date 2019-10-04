function varargout = AudioFileProcessor(varargin)
% AUDIOFILEPROCESSOR MATLAB code for AudioFileProcessor.fig
%   GUI to processes audio files (.wav format)
%   Processed stimuli is sent to the RF coils via USB/UART
%      AUDIOFILEPROCESSOR, by itself, creates a new AUDIOFILEPROCESSOR or raises the existing
%      singleton*.
%
%      H = AUDIOFILEPROCESSOR returns the handle to a new AUDIOFILEPROCESSOR or the handle to
%      the existing singleton*.
%
%      AUDIOFILEPROCESSOR('CALLBACK',hObject,eventData,handles,...) calls the local
%      function named CALLBACK in AUDIOFILEPROCESSOR.M with the given input arguments.
%
%      AUDIOFILEPROCESSOR('Property','Value',...) creates a new AUDIOFILEPROCESSOR or raises the
%      existing singleton*.  Starting from the left, property value pairs are
%      applied to the GUI before AudioFileProcessor_OpeningFcn gets called.  An
%      unrecognized property name or invalid value makes property application
%      stop.  All inputs are passed to AudioFileProcessor_OpeningFcn via varargin.
%
%      *See GUI Options on GUIDE's Tools menu.  Choose "GUI allows only one
%      instance to run (singleton)".
%
% See also: GUIDE, GUIDATA, GUIHANDLES

% Edit the above text to modify the response to help AudioFileProcessor

% Last Modified by GUIDE v2.5 13-Oct-2016 21:54:22

% Begin initialization code - DO NOT EDIT
gui_Singleton = 1;
gui_State = struct('gui_Name',       mfilename, ...
    'gui_Singleton',  gui_Singleton, ...
    'gui_OpeningFcn', @AudioFileProcessor_OpeningFcn, ...
    'gui_OutputFcn',  @AudioFileProcessor_OutputFcn, ...
    'gui_LayoutFcn',  [] , ...
    'gui_Callback',   []);
if nargin && ischar(varargin{1})
    gui_State.gui_Callback = str2func(varargin{1});
end

if nargout
    [varargout{1:nargout}] = gui_mainfcn(gui_State, varargin{:});
else
    gui_mainfcn(gui_State, varargin{:});
end
% End initialization code - DO NOT EDIT


% --- Executes just before AudioFileProcessor is made visible.
function AudioFileProcessor_OpeningFcn(hObject, eventdata, handles, varargin)
% This function has no output args, see OutputFcn.
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% varargin   command line arguments to AudioFileProcessor (see VARARGIN)

% Choose default command line output for AudioFileProcessor
handles.output = hObject;

% add common functions path to the current directory
currentFolder = pwd; CCIMobileFolder = fileparts(currentFolder); %currentFolder(1:end-18);
CommonFunctionsFolder = [CCIMobileFolder '\CommonFunctions\'];
addpath(CommonFunctionsFolder);
handles.start_dir = currentFolder; handles.current_dir = currentFolder;
guidata(hObject, handles);

p = initialize_ACE;
handles.parameters = p;

% Populate the listbox
dir_struct = dir(currentFolder);
[sorted_names,sorted_index] = sortrows({dir_struct.name}');
handles.file_names = sorted_names;
handles.is_dir = [dir_struct.isdir];
handles.sorted_index = sorted_index;
guidata(hObject, handles);
guidata(handles.figure1,handles);
set(handles.listbox1,'String',handles.file_names,'Value',1);
set(handles.text1,'String',currentFolder);
if (isfield(p,'Left')==1)
    p.General.LeftOn = 1; handles.parameters.General.LeftOn = 1;
    set(handles.slider_lgain,'Value', p.Left.Gain);
    set(handles.text_lgain, 'String', ['Gain = ' num2str( p.Left.Gain) 'dB']);
else
    p.General.LeftOn = 0; handles.parameters.General.LeftOn = 0;
    set(handles.slider_lgain,'Enable','off');
    set(handles.text_lgain, 'Enable','off');
end
if (isfield(p,'Right')==1)
    p.General.RightOn = 1; handles.parameters.General.RightOn = 1;
    set(handles.slider_rgain,'Value', p.Right.Gain);
    set(handles.text_rgain, 'String', ['Gain = ' num2str( p.Right.Gain) 'dB']);
else
    p.General.RightOn = 0; handles.parameters.General.RightOn = 0;
    set(handles.slider_rgain,'Enable','off');
    set(handles.text_rgain, 'Enable','off');
end
text_msg = ['Subject Name: ' handles.parameters.General.SubjectName '    Subject ID: ' handles.parameters.General.SubjectID '     MAP: ' handles.parameters.General.MapTitle];
set(handles.text_map, 'String', text_msg);
% Update handles structure
guidata(hObject, handles);

% UIWAIT makes AudioFileProcessor wait for user response (see UIRESUME)
% uiwait(handles.figure1);


% --- Outputs from this function are returned to the command line.
function varargout = AudioFileProcessor_OutputFcn(hObject, eventdata, handles)
% varargout  cell array for returning output args (see VARARGOUT);
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Get default command line output from handles structure
varargout{1} = handles.output;

function load_listbox(handles, hObject)
dir_path = handles.current_dir;
dir_struct = dir(dir_path);
[sorted_names,sorted_index] = sortrows({dir_struct.name}');
handles.file_names = sorted_names;
handles.is_dir = [dir_struct.isdir];
handles.sorted_index = sorted_index;
set(handles.listbox1,'String',handles.file_names,'Value',1);
set(handles.text1,'String',dir_path);
% % Update handles structure
guidata(hObject, handles); guidata(handles.figure1,handles);

% --- Executes on selection change in listbox1.
function listbox1_Callback(hObject, eventdata, handles)
% hObject    handle to listbox1 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
index_selected = get(handles.listbox1,'Value');
file_list = get(handles.listbox1,'String');
filename = file_list{index_selected};

if strcmp(get(handles.figure1,'SelectionType'),'normal')
    %     current_dir=pwd;
    %     handles.current_dir = current_dir;
    %     guidata(hObject,handles);
end

if strcmp(get(handles.figure1,'SelectionType'),'open')
    if  handles.is_dir(handles.sorted_index(index_selected))
        if strcmp(filename,'..')
            filename = fileparts(handles.current_dir);
        else
            filename = [handles.current_dir '\' filename];
        end
        handles.current_dir = filename;
        guidata(hObject,handles);
        load_listbox(handles, hObject);
    else
        [path,name,ext,ver] = fileparts(filename);
        switch ext
            case '.fig'
                guide (filename)
            otherwise
                try
                    open(filename)
                catch
                    errordlg(lasterr,'File Type Error','modal')
                end
        end
    end
end
% Hints: contents = cellstr(get(hObject,'String')) returns listbox1 contents as cell array
%        contents{get(hObject,'Value')} returns selected item from listbox1

% --- Executes during object creation, after setting all properties.
function listbox1_CreateFcn(hObject, eventdata, handles)
% hObject    handle to listbox1 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: listbox controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end

% --- Executes on slider movement.
function slider_rgain_Callback(hObject, eventdata, handles)
% hObject    handle to slider_rgain (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
new_value = get(hObject,'Value');
drawnow;     handles = guidata(hObject);
handles.parameters.Right.Gain = new_value;
handles.parameters.Right.gains_dB = handles.parameters.Right.Gain + handles.parameters.Right.BandGains;
handles.parameters.Right.gains = 10 .^ (handles.parameters.Right.gains_dB / 20.0);  %handles.parameters.right.gains(23-handles.parameters.right.off_electrodes)=[];
set(handles.text_rgain, 'String', ['Gain = ' num2str(handles.parameters.Right.Gain) 'dB']);
guidata(hObject, handles);
% Hints: get(hObject,'Value') returns position of slider
%        get(hObject,'Min') and get(hObject,'Max') to determine range of slider


% --- Executes during object creation, after setting all properties.
function slider_rgain_CreateFcn(hObject, eventdata, handles)
% hObject    handle to slider_rgain (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: slider controls usually have a light gray background.
if isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor',[.9 .9 .9]);
end


% --- Executes on slider movement.
function slider_lgain_Callback(hObject, eventdata, handles)
% hObject    handle to slider_lgain (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
new_value = get(hObject,'Value');
drawnow;     handles = guidata(hObject);
handles.parameters.Left.Gain = new_value;
handles.parameters.Left.gains_dB = handles.parameters.Left.Gain + handles.parameters.Left.BandGains;
handles.parameters.Left.gains = 10 .^ (handles.parameters.Left.gains_dB / 20.0);  %handles.parameters.Left.gains(23-handles.parameters.left.off_electrodes)=[];
set(handles.text_lgain, 'String', ['Gain = ' num2str(handles.parameters.Left.Gain) 'dB']);
guidata(hObject, handles);
% Hints: get(hObject,'Value') returns position of slider
%        get(hObject,'Min') and get(hObject,'Max') to determine range of slider


% --- Executes during object creation, after setting all properties.
function slider_lgain_CreateFcn(hObject, eventdata, handles)
% hObject    handle to slider_lgain (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: slider controls usually have a light gray background.
if isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor',[.9 .9 .9]);
end

function disable_buttons(handles)
set(handles.listbox1,'Enable','off');
set(handles.slider_lgain,'Enable','off');
set(handles.slider_rgain,'Enable','off');
set(handles.pushbutton_repeat,'Enable','off');
set(handles.pushbutton_play,'Enable','off');
set(handles.pushbutton_next,'Enable','off');

function enable_buttons(handles)
set(handles.listbox1,'Enable','on');
set(handles.slider_lgain,'Enable','on');
set(handles.slider_rgain,'Enable','on');
set(handles.pushbutton_repeat,'Enable','on');
set(handles.pushbutton_play,'Enable','on');
set(handles.pushbutton_next,'Enable','on');

% --- Executes on button press in pushbutton_repeat.
function pushbutton_repeat_Callback(hObject, eventdata, handles)
% hObject    handle to pushbutton_repeat (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
set(handles.figure1, 'HandleVisibility', 'off');
close all; % close all other figures (e.g., electrodograms) but not this GUI
set(handles.figure1, 'HandleVisibility', 'on');
disable_buttons(handles);

index_selected = get(handles.listbox1,'Value');
new_index=index_selected-1;
% if (new_index<handles.listbox1.Value)
%     new_index = handles.listbox1.Value;
% end

set(handles.listbox1,'Value',new_index);
file_list = get(handles.listbox1,'String');
filename = cell2mat(file_list(new_index));
faddress=[handles.current_dir '\' filename];
[pathstr,name,extension] = fileparts(faddress);
if (strcmp(extension,'.wav')==1)
    stimulate(faddress, handles.parameters);
    %[y,Fs] = audioread(faddress);sound(y,Fs);
else
    errordlg('Only .wav files are supported', 'Incorrect File type');
end
enable_buttons(handles);
set(handles.listbox1,'Value',new_index);

% --- Executes on button press in pushbutton_play.
function pushbutton_play_Callback(hObject, eventdata, handles)
% hObject    handle to pushbutton_play (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
set(handles.figure1, 'HandleVisibility', 'off');
close all; % close all other figures (e.g., electrodograms) but not this GUI
set(handles.figure1, 'HandleVisibility', 'on');
disable_buttons(handles);

index_selected = get(handles.listbox1,'Value');
file_list = get(handles.listbox1,'String');
filename = cell2mat(file_list(index_selected));
faddress=[handles.current_dir '\' filename];
[pathstr,name,extension] = fileparts(faddress);
if (strcmp(extension,'.wav')==1)
    stimulate(faddress, handles.parameters);
    %[y,Fs] = audioread(faddress);sound(y,Fs);
else
    errordlg('Only .wav files are supported', 'Incorrect File type');
end
enable_buttons(handles);
set(handles.listbox1,'String',handles.file_names, 'Value',index_selected)

% --- Executes on button press in pushbutton_next.
function pushbutton_next_Callback(hObject, eventdata, handles)
% hObject    handle to pushbutton_next (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
set(handles.figure1, 'HandleVisibility', 'off');
close all; % close all other figures (e.g., electrodograms) but not this GUI
set(handles.figure1, 'HandleVisibility', 'on');
disable_buttons(handles);

index_selected = get(handles.listbox1,'Value');
new_index=index_selected+1;
% if (new_index>handles.listbox1.Value)
%     new_index = handles.listbox1.Value;
% end
set(handles.listbox1,'Value',new_index);
file_list = get(handles.listbox1,'String');
filename = cell2mat(file_list(new_index));
faddress=[handles.current_dir '\' filename];
[pathstr,name,extension] = fileparts(faddress);
if (strcmp(extension,'.wav')==1)
    stimulate(faddress, handles.parameters);
    %[y,Fs] = audioread(faddress);sound(y,Fs);
else
    errordlg('Only .wav files are supported', 'Incorrect File type');
end
enable_buttons(handles);
set(handles.listbox1,'Value',new_index);