function varargout = AudioScope(varargin)

% AudioScope
% GUI to visualize audio signal from the BTE in realtime

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Copyright: CRSS-CILab, UT-Dallas
%   Authors: Hussnain Ali
%      Date: 2016/06/18
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


% AUDIOSCOPE MATLAB code for AudioScope.fig
%      AUDIOSCOPE, by itself, creates a new AUDIOSCOPE or raises the existing
%      singleton*.
%
%      H = AUDIOSCOPE returns the handle to a new AUDIOSCOPE or the handle to
%      the existing singleton*.
%
%      AUDIOSCOPE('CALLBACK',hObject,eventData,handles,...) calls the local
%      function named CALLBACK in AUDIOSCOPE.M with the given input arguments.
%
%      AUDIOSCOPE('Property','Value',...) creates a new AUDIOSCOPE or raises the
%      existing singleton*.  Starting from the left, property value pairs are
%      applied to the GUI before AudioScope_OpeningFcn gets called.  An
%      unrecognized property name or invalid value makes property application
%      stop.  All inputs are passed to AudioScope_OpeningFcn via varargin.
%
%      *See GUI Options on GUIDE's Tools menu.  Choose "GUI allows only one
%      instance to run (singleton)".
%
% See also: GUIDE, GUIDATA, GUIHANDLES

% Edit the above text to modify the response to help AudioScope

% Last Modified by GUIDE v2.5 05-Nov-2015 21:34:40

% Begin initialization code - DO NOT EDIT
gui_Singleton = 1;
gui_State = struct('gui_Name',       mfilename, ...
    'gui_Singleton',  gui_Singleton, ...
    'gui_OpeningFcn', @AudioScope_OpeningFcn, ...
    'gui_OutputFcn',  @AudioScope_OutputFcn, ...
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


% --- Executes just before AudioScope is made visible.
function AudioScope_OpeningFcn(hObject, eventdata, handles, varargin)
% This function has no output args, see OutputFcn.
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% varargin   command line arguments to AudioScope (see VARARGIN)

% Choose default command line output for AudioScope
handles.output = hObject;
global fs; fs = 16000;
% add common functions path to the current directory
currentFolder = pwd; CCIMobileFolder = fileparts(currentFolder); %currentFolder(1:end-10);
CommonFunctionsFolder = [CCIMobileFolder '\CommonFunctions\'];
addpath(CommonFunctionsFolder);

% Update handles structure
guidata(hObject, handles);

% UIWAIT makes AudioScope wait for user response (see UIRESUME)
% uiwait(handles.figure1);


% --- Outputs from this function are returned to the command line.
function varargout = AudioScope_OutputFcn(hObject, eventdata, handles)
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
global stop; global s;
stop = 1;
delete(s); clear s;
set(handles.buttonStart,'Enable','on');


% --- Executes on button press in buttonStart.
function buttonStart_Callback(hObject, eventdata, handles)
% hObject    handle to buttonStart (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
global stop; global s; global fs;
stop = 0; 
set(handles.buttonStart,'Enable','off');
s = initializeBoard;
hAx = handles.axes1;
maxA = 4000;  minA = -4000;
index = 1; a = zeros(1,16000); nSamples = numel(a); %round(fs*timeBase);
hLine = plot(hAx,(1:nSamples)/fs,a(:,index:index+nSamples-1));
xlabel('time');
ylim([minA maxA]);
xlim([0 nSamples/fs]);
dummy_output_buffer = UART_start_buffer;

while (stop==0)
    if Wait(s)>= 512
        AD_data_bytes = Read(s, 512);
        AD_data=typecast(int8(AD_data_bytes), 'int16');
        left = AD_data(1:2:end); %right=AD_data(2:2:end);
        
        a(nSamples-127:end) = left; %Plot left side only
        set(hLine,'ydata',a); %(:,index:index+128-1));
        drawnow  %updates the display
        a(1:nSamples-128) = a(129:nSamples);
        
        if isvalid(s)
            Write(s, dummy_output_buffer,516);
        end
        
    end
    
end
