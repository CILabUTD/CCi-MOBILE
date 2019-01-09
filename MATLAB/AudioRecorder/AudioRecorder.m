function varargout = AudioRecorder(varargin)

% AudioRecorder
% Records the stereo audio signal from the BTE and stores into wave file
% 'BTEaudio.wav'

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright: CRSS-CILab, UT-Dallas
%   Authors: Hussnain Ali
%      Date: 2016/06/18
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% AUDIORECORDER MATLAB code for AudioRecorder.fig
%      AUDIORECORDER, by itself, creates a new AUDIORECORDER or raises the existing
%      singleton*.
%
%      H = AUDIORECORDER returns the handle to a new AUDIORECORDER or the handle to
%      the existing singleton*.
%
%      AUDIORECORDER('CALLBACK',hObject,eventData,handles,...) calls the local
%      function named CALLBACK in AUDIORECORDER.M with the given input arguments.
%
%      AUDIORECORDER('Property','Value',...) creates a new AUDIORECORDER or raises the
%      existing singleton*.  Starting from the left, property value pairs are
%      applied to the GUI before AudioRecorder_OpeningFcn gets called.  An
%      unrecognized property name or invalid value makes property application
%      stop.  All inputs are passed to AudioRecorder_OpeningFcn via varargin.
%
%      *See GUI Options on GUIDE's Tools menu.  Choose "GUI allows only one
%      instance to run (singleton)".
%
% See also: GUIDE, GUIDATA, GUIHANDLES

% Edit the above text to modify the response to help AudioRecorder

% Last Modified by GUIDE v2.5 06-Nov-2015 16:10:32

% Begin initialization code - DO NOT EDIT
gui_Singleton = 1;
gui_State = struct('gui_Name',       mfilename, ...
                   'gui_Singleton',  gui_Singleton, ...
                   'gui_OpeningFcn', @AudioRecorder_OpeningFcn, ...
                   'gui_OutputFcn',  @AudioRecorder_OutputFcn, ...
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


% --- Executes just before AudioRecorder is made visible.
function AudioRecorder_OpeningFcn(hObject, eventdata, handles, varargin)
% This function has no output args, see OutputFcn.
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% varargin   command line arguments to AudioRecorder (see VARARGIN)

% Choose default command line output for AudioRecorder
handles.output = hObject;
global fs; fs = 16000;
% add common functions path to the current directory
currentFolder = pwd; CCIMobileFolder = fileparts(currentFolder); %currentFolder(1:end-13);
CommonFunctionsFolder = [CCIMobileFolder '\CommonFunctions\'];
addpath(CommonFunctionsFolder);

set(handles.buttonPlay,'Enable','off');
set(handles.buttonStop,'Enable','off');
set(handles.textStatus,'String','Press Record to start recording');

im = imread('mic.png');
imshow(im, 'Parent', handles.axes1);
% Update handles structure
guidata(hObject, handles);

% UIWAIT makes AudioRecorder wait for user response (see UIRESUME)
% uiwait(handles.figure1);


% --- Outputs from this function are returned to the command line.
function varargout = AudioRecorder_OutputFcn(hObject, eventdata, handles) 
% varargout  cell array for returning output args (see VARARGOUT);
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Get default command line output from handles structure
varargout{1} = handles.output;


% --- Executes on button press in buttonStop.
function buttonStop_Callback(hObject, eventdata, handles)
% hObject    handle to buttonStop (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
global stop;
stop = 1;
set(handles.buttonPlay,'Enable','on');
set(handles.buttonStop,'Enable','off');
set(handles.buttonRecord,'Enable','on');
set(handles.textStatus,'String',' ');

% --- Executes on button press in buttonRecord.
function buttonRecord_Callback(hObject, eventdata, handles)
% hObject    handle to buttonRecord (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
if exist('BTEaudio.wav', 'file') == 2
    delete BTEaudio.wav; 
end
set(handles.buttonPlay,'Enable','off');
set(handles.buttonRecord,'Enable','off');
set(handles.buttonStop,'Enable','on');
drawnow; set(handles.textStatus,'String','Recording');

global stop; global s; global fs; %global audio_stream;
stop = 0; 
s = initializeBoard;
dummy_output_buffer = UART_start_buffer;
audio_stream = []; i = 0;
while (stop==0)
    if Wait(s)>= 512
        AD_data_bytes = Read(s, 512);
        AD_data=typecast(int8(AD_data_bytes), 'int16');
        left = double(AD_data(1:2:end)); right=double(AD_data(2:2:end));
        stereo_signal = [left;right];
        % Following is a slower method and only good for short audio
        % recordings; consider pre-allocationg audio stream for specific
        % number of frames for efficiency
        audio_stream = [audio_stream stereo_signal];
        if isvalid(s)
            Write(s, dummy_output_buffer,516);
        end
        t = floor(i*0.008); msg = ['Recording: ' num2str(t) ' s'];
        drawnow; set(handles.textStatus,'String', msg);
        clear left; clear right; clear stereo_signal;
        i = i+1;
    end
end
drawnow; set(handles.textStatus,'String','Recording Stopped');  
if isempty(audio_stream)==0
    audiowrite('BTEaudio.wav',(audio_stream.*7e-5)',fs);
else
    disp('Audio Stream is empty');
end
delete(s); clear s;

% --- Executes on button press in buttonPlay.
function buttonPlay_Callback(hObject, eventdata, handles)
% hObject    handle to buttonPlay (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
set(handles.buttonRecord,'Enable','off');
set(handles.buttonStop,'Enable','off');
set(handles.buttonPlay,'Enable','off');
set(handles.textStatus,'String','Playing');
global stop;
stop = 0;
if exist('BTEaudio.wav', 'file') == 2
    disp('PLAYING');
    [y, SamplingRate] = audioread('BTEaudio.wav');
    sound(y,SamplingRate);
    clear y SamplingRate;
else
    disp('No recording exists');
end

pause(2);
set(handles.buttonPlay,'Enable','on');
set(handles.buttonRecord,'Enable','on');
set(handles.textStatus,'String','  ');
