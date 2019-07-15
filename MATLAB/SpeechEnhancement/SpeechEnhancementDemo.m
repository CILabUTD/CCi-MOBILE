function varargout = SpeechEnhancementDemo(varargin)
% THE CCI-MOBILE Speech Enhancement
%  -------------------------------------------------------------------------
%
% -----------------------------------------------------------------------
% INPUTS: -------------------------------------------------------------------------
% Audio Signal (.wav file)

% OUTPUTS: -------------------------------------------------------------------------
% Enhanced  signal
  
% FILE ASSOCIATIONS

% REFERENCES:
% 
% [1] Nursadul Mamun, Soheil Khorram, John H.L. Hansen “Convolutional Neural Network-based Speech Enhancement 
% for Cochlear Implant Recipients, INTERSPEECH 2019,  15-19 September 2019,  Graz, Austria. (International) 
%
     
% -------------------------------------------------------------------------
%
% Author(s): Nursadul Mamun, J. H. Hansen 
% Date     : June 30, 2019
% Contact  : (nursadul.mamun, john.hansen)@utdallas.edu
% University of Texas at Dallas, USA
% -------------------------------------------------------------------------

%
%      *See GUI Options on GUIDE's Tools menu.  Choose "GUI allows only one
%      instance to run (singleton)".
%
% See also: GUIDE, GUIDATA, GUIHANDLES

% Edit the above text to modify the response to help SpeechEnhancementDemo

% Last Modified by GUIDE v2.5 12-Jul-2019 09:27:17

% Begin initialization code - DO NOT EDIT
gui_Singleton = 1;
gui_State = struct('gui_Name',       mfilename, ...
    'gui_Singleton',  gui_Singleton, ...
    'gui_OpeningFcn', @SpeechEnhancementDemo_OpeningFcn, ...
    'gui_OutputFcn',  @SpeechEnhancementDemo_OutputFcn, ...
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


% --- Executes just before SpeechEnhancementDemo is made visible.
function SpeechEnhancementDemo_OpeningFcn(hObject, eventdata, handles, varargin)
% This function has no output args, see OutputFcn.
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% varargin   command line arguments to SpeechEnhancementDemo (see VARARGIN)

% Choose default command line output for SpeechEnhancementDemo
handles.output = hObject;

% update handles structure
guidata(hObject, handles);
axes(handles.FigureHeader)
imshow('Header.png')

% UIWAIT makes SpeechEnhancementDemo wait for user response (see UIRESUME)
% uiwait(handles.figure1);


% --- Outputs from this function are returned to the command line.
function varargout = SpeechEnhancementDemo_OutputFcn(hObject, eventdata, handles)
% varargout  cell array for returning output args (see VARARGOUT);
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Get default command line output from handles structure
varargout{1} = handles.output;


% --- Executes on button press in ButtonLoadAudio.
function ButtonLoadAudio_Callback(hObject, eventdata, handles)
% hObject    handle to ButtonLoadAudio (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
[FileName,PathName] = uigetfile('*.wav','Select your file');
Fullpathname = strcat(PathName,FileName);
%  set(handles.edit5,'string',FileName); % Showing Full path name
[signal,Fs] = audioread(Fullpathname);
signal = signal/max(abs(signal)); % normalize
handles.signal = signal;
handles.Fs = Fs;
guidata(hObject,handles);





% --- Executes on button press in Update.
function Update_Callback(hObject, eventdata, handles)

%%%%%  Current Directory %%%%%%%%%%%%%
currentFolder = pwd;

%%%%%%%%%%%%%%%% Get the Input signal From the handles %%%%%%%%%%
signal = handles.signal;
Fs = handles.Fs;
t = (1:numel(signal))./Fs;

%%%%%%%%%%%%%% Noise type Selection %%%%%%
Selection = get(handles.Noise_Selection,'Selectedobject');
Noisy_type = get(Selection,'string');

%%%%%%%%%%%%%% SNR value Selection %%%%%%
SNR_value = get(handles.SNR_Selection,'Selectedobject');
SNR = get(SNR_value,'string');
SNR = str2num(SNR);

%%%%%%%%%%%%%% Enhanced Method  Selection %%%%%%
Enhancement_type = get(handles.Enhancement_Method,'Selectedobject');
Enhancement_type = get(Enhancement_type,'string');

%%%%%%%%%%%%%%%%%%  Noisy Signal %%%%%%%%%%%%%


if strcmp(Noisy_type,'Babble')
    
    Data_folder = [currentFolder '\Noise\'];
    [noise F_n] = audioread([Data_folder 'Babble_Noise.wav']);
    noise = resample(noise,Fs,F_n);
    if length(noise)<=length(signal)
        noise = [noise;noise] ;
    end
    Noisy_signal=addnoise_snr(signal, Fs, 16,noise, Fs, 16,SNR);
    
    
elseif strcmp(Noisy_type,'White Gaussian')
    
    Noisy_signal=awgn(signal,SNR,'measured');
    noise = Noisy_signal - signal;
    
elseif strcmp(Noisy_type,'SS')
    Data_folder = [currentFolder '\Noise\'];
    [noise F_n] = audioread([Data_folder 'Speech_shaped_noise.wav']);
    noise = resample(noise,Fs,F_n);
    
    if length(noise)<=length(signal)
        noise = [noise;noise] ;
    end
    
    Noisy_signal=addnoise_snr(signal, Fs, 16,noise, Fs, 16,SNR);
    
end


handles.noise=noise;
handles.Noisy_signal = Noisy_signal;

%%%%%%%%%%%%%%%%%%  Enhencement method  %%%%%%%%%%%%%

Enhencement_Method_Folder = [currentFolder  '\Common Speech Enhancement Algorithm'];

addpath (Enhencement_Method_Folder)
if strcmp(Enhancement_type,'Weiner Filtering')
    
    Enhanced_Signal = wiener_wt(Noisy_signal,Fs);
elseif strcmp(Enhancement_type,'LogMMSE')
    
    Enhanced_Signal = logmmse(Noisy_signal,Fs);
    
    % elseif strcmp(Enhancement_type,'mm')
    
end

handles.Enhanced_Signal = Enhanced_Signal;

%% Plotting Noisy Signal and its Spectrogram %%%%%%%

%%% Noisy
axes(handles.FigureNoisyAudio)
signal_axis = linspace(0, length(signal)/Fs, length(Noisy_signal));
plot(signal_axis, Noisy_signal);
title('Noisy Signal')
xlabel('Time (s)')
ylabel('Amplitude')
%%% Enhanced
axes(handles.FigureEnhancedAudio)
signal_axis = linspace(0, length(signal)/Fs, length(Enhanced_Signal));
plot(signal_axis, Enhanced_Signal);
title('Enhanced Signal')
xlabel('Time (s)')
ylabel('Amplitude')

%%  Plotting Spectrogram %%%%%%%

%%%Noisy
axes(handles.FigureNoisySpectrogram)
spectrogram(Noisy_signal,300,[],[],Fs,'yaxis')
title('Enhanced Spectrogram')
%%% Enhanced %%%
axes(handles.FigureEnhancedSpectrogram)
spectrogram(Enhanced_Signal,300,[],[],Fs,'yaxis')
title('Enhanced Spectrogram')


%%  Plotting Electrodogram %%%%%%%
CCiMobile_path = [currentFolder  '\CCi Mobile'];
load([currentFolder '\p.mat'])
%%%Noisy
addpath (CCiMobile_path)
[~,p_noisy,q_noisy ]= ACE_Process_CI(Noisy_signal, p,Fs);
 
FigureNoisyElectrodogram = handles.FigureNoisyElectrodogram;
 
plot_electrodogram(q_noisy,FigureNoisyElectrodogram,['Electrodogram: ' p_noisy.lr_select ' ear']);
title('Noisy Electrodogram')

%%% Enhanced %%
cd(currentFolder)
addpath (CCiMobile_path)
[~,p_enhanced,q_enhanced ]= ACE_Process_CI(Enhanced_Signal, p,Fs);
FigureEnhancedElectrodogram = handles.FigureEnhancedElectrodogram;
plot_electrodogram(q_enhanced,FigureEnhancedElectrodogram,['Electrodogram: ' p_enhanced.lr_select ' ear']);
title('Noisy Electrodogram')
cd(currentFolder)
guidata(hObject,handles);





% --- Executes on button press in PlayCleanSignal.
function PlayCleanSignal_Callback(hObject, eventdata, handles)
% hObject    handle to PlayCleanSignal (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
signal=handles.signal;
Fs=handles.Fs;
sound(signal,Fs)


% --- Executes on button press in PlayEnhanced.
function PlayEnhanced_Callback(hObject, eventdata, handles)
% hObject    handle to PlayEnhanced (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
Enhanced_Signal=handles.Enhanced_Signal;
Fs=handles.Fs;
sound(Enhanced_Signal,Fs)


% --- Executes on button press in PlayNoisySignal.
function PlayNoisySignal_Callback(hObject, eventdata, handles)
% hObject    handle to PlayNoisySignal (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
Noisy_signal=handles.Noisy_signal;
Fs=handles.Fs;
sound(Noisy_signal,Fs)


% --- Executes on button press in PlayNoise.
function PlayNoise_Callback(hObject, eventdata, handles)
% hObject    handle to PlayNoise (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
noise=handles.noise;
Fs=handles.Fs;
sound(noise,Fs)


% --- Executes on button press in radiobutton9.
function radiobutton9_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton9 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton9


% --- Executes on button press in radiobutton10.
function radiobutton10_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton10 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton10


% --- Executes on button press in radiobutton11.
function radiobutton11_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton11 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton11


% --- Executes on button press in radiobutton1.
function radiobutton1_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton1 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton1


% --- Executes on button press in radiobutton7.
function radiobutton7_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton7 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton7


% --- Executes on button press in radiobutton8.
function radiobutton8_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton8 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton8


% --- Executes on button press in radiobutton12.
function radiobutton12_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton12 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton12


% --- Executes on button press in radiobutton16.
function radiobutton16_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton16 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton16


% --- Executes on button press in radiobutton17.
function radiobutton17_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton17 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton17


% --- Executes on button press in radiobutton18.
function radiobutton18_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton18 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton18


% --- Executes on button press in radiobutton13.
function radiobutton13_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton13 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton13


% --- Executes on button press in radiobutton14.
function radiobutton14_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton14 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton14


% --- Executes on button press in radiobutton15.
function radiobutton15_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton15 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton15


% --- Executes on button press in radiobutton19.
function radiobutton19_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton19 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton19


% --- Executes on button press in radiobutton20.
function radiobutton20_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton20 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton20


% --- Executes on button press in radiobutton21.
function radiobutton21_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton21 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton21


% --- Executes during object creation, after setting all properties.
function Noise_Selection_CreateFcn(hObject, eventdata, handles)
% hObject    handle to Noise_Selection (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called


% --- Executes on button press in radiobutton22.
function radiobutton22_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton22 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton22


% --- Executes on button press in radiobutton23.
function radiobutton23_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton23 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton23
