function varargout = RealtimeStimulator(varargin)
% REALTIMESTIMULATOR MATLAB code for RealtimeStimulator.fig
%      REALTIMESTIMULATOR, by itself, creates a new REALTIMESTIMULATOR or raises the existing
%      singleton*.
%
%      H = REALTIMESTIMULATOR returns the handle to a new REALTIMESTIMULATOR or the handle to
%      the existing singleton*.
%
%      REALTIMESTIMULATOR('CALLBACK',hObject,eventData,handles,...) calls the local
%      function named CALLBACK in REALTIMESTIMULATOR.M with the given input arguments.
%
%      REALTIMESTIMULATOR('Property','Value',...) creates a new REALTIMESTIMULATOR or raises the
%      existing singleton*.  Starting from the left, property value pairs are
%      applied to the GUI before RealtimeStimulator_OpeningFcn gets called.  An
%      unrecognized property name or invalid value makes property application
%      stop.  All inputs are passed to RealtimeStimulator_OpeningFcn via varargin.
%
%      *See GUI Options on GUIDE's Tools menu.  Choose "GUI allows only one
%      instance to run (singleton)".
%
% See also: GUIDE, GUIDATA, GUIHANDLES

% Edit the above text to modify the response to help RealtimeStimulator

% Last Modified by GUIDE v2.5 06-Oct-2016 22:51:51

% Begin initialization code - DO NOT EDIT
gui_Singleton = 1;
gui_State = struct('gui_Name',       mfilename, ...
    'gui_Singleton',  gui_Singleton, ...
    'gui_OpeningFcn', @RealtimeStimulator_OpeningFcn, ...
    'gui_OutputFcn',  @RealtimeStimulator_OutputFcn, ...
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


% --- Executes just before RealtimeStimulator is made visible.
function RealtimeStimulator_OpeningFcn(hObject, eventdata, handles, varargin)
% This function has no output args, see OutputFcn.
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% varargin   command line arguments to RealtimeStimulator (see VARARGIN)

% Choose default command line output for RealtimeStimulator
handles.output = hObject;

global fs; fs = 16000;

% add common functions path to the current directory
currentFolder = pwd; CCIMobileFolder = fileparts(currentFolder); %currentFolder(1:end-4);
CommonFunctionsFolder = [CCIMobileFolder '\CommonFunctions\'];
addpath(CommonFunctionsFolder);

p = initialize_ACE_integer_ppf;
handles.parameters = p;
handles.stop = 0;

if (isfield(p,'Left')==1)
    p.General.LeftOn = 1; handles.parameters.General.LeftOn = 1;
    set(handles.slider_sensitivity_left,'Value', p.Left.Sensitivity );
    set(handles.slider_gain_left,'Value', p.Left.Gain);
    set(handles.slider_volume_right,'Value', p.Left.Volume);
    set(handles.text_sens_left, 'String', ['Sensitivity = ' num2str(p.Left.Sensitivity)]);
    set(handles.text_gain_left, 'String', ['Gain = ' num2str( p.Left.Gain) 'dB']);
    set(handles.text_vol_left, 'String', ['Volume = ' num2str(p.Left.Volume)]);
    set(handles.checkbox_left, 'Value', p.General.LeftOn);
else
    p.General.LeftOn = 0; handles.parameters.General.LeftOn = 0;
    set(handles.slider_sensitivity_left,'Enable','off' );
    set(handles.slider_gain_left,'Enable','off');
    set(handles.slider_volume_right,'Enable','off');
    set(handles.text_sens_left, 'Enable','off');
    set(handles.text_gain_left, 'Enable','off');
    set(handles.text_vol_left, 'Enable','off');
    set(handles.checkbox_left, 'Enable','off');
end
if (isfield(p,'Right')==1)
    p.General.RightOn = 1; handles.parameters.General.RightOn = 1;
    set(handles.slider_sensitivity_right,'Value', p.Right.Sensitivity);
    set(handles.slider_gain_right,'Value', p.Right.Gain);
    set(handles.slider_volume_right,'Value', p.Right.Volume);
    set(handles.text_sens_right, 'String', ['Sensitivity = ' num2str(p.Right.Sensitivity)]);
    set(handles.text_gain_right, 'String', ['Gain = ' num2str( p.Right.Gain) 'dB']);
    set(handles.text_vol_right, 'String', ['Volume = ' num2str(p.Right.Volume)]);
    set(handles.checkbox_right, 'Value', p.General.RightOn);
else
    p.General.RightOn = 0; handles.parameters.General.RightOn = 0;
    set(handles.slider_sensitivity_right,'Enable','off');
    set(handles.slider_gain_right,'Enable','off');
    set(handles.slider_volume_right,'Enable','off');
    set(handles.text_sens_right, 'Enable','off');
    set(handles.text_gain_right, 'Enable','off');
    set(handles.text_vol_right, 'Enable','off');
    set(handles.checkbox_right, 'Enable','off');
end

axisHandle = handles.axes4;
axes(axisHandle)
matlabImage = imread('interface.jpg');
imshow(matlabImage, 'XData', [1 1]);
%image(matlabImage)
%axis off
%axis imag

% Update handles structure
guidata(hObject, handles);


% UIWAIT makes RealtimeStimulator wait for user response (see UIRESUME)
% uiwait(handles.figure1);


% --- Outputs from this function are returned to the command line.
function varargout = RealtimeStimulator_OutputFcn(hObject, eventdata, handles)
% varargout  cell array for returning output args (see VARARGOUT);
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Get default command line output from handles structure
varargout{1} = handles.output;

% --- Executes on button press in checkbox_left.
function checkbox_left_Callback(hObject, eventdata, handles)
% hObject    handle to checkbox_left (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
new_value = get(hObject,'Value');
drawnow;     handles = guidata(hObject);
handles.parameters.General.LeftOn = new_value; guidata(hObject, handles);
if new_value==0
    set(handles.slider_sensitivity_left,'Enable','off' );
    set(handles.slider_gain_left,'Enable','off');
    set(handles.slider_volume_right,'Enable','off');
    set(handles.text_sens_left, 'Enable','off');
    set(handles.text_gain_left, 'Enable','off');
    set(handles.text_vol_left, 'Enable','off');
else
    set(handles.slider_sensitivity_left,'Enable','on' );
    set(handles.slider_gain_left,'Enable','on');
    set(handles.slider_volume_right,'Enable','on');
    set(handles.text_sens_left, 'Enable','on');
    set(handles.text_gain_left, 'Enable','on');
    set(handles.text_vol_left, 'Enable','on');
end
% Hint: get(hObject,'Value') returns toggle state of checkbox_left

% --- Executes during object creation, after setting all properties.
function checkbox_left_CreateFcn(hObject, eventdata, handles)
% hObject    handle to checkbox_left (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called
handles.parameters.General.LeftOn = 1; guidata(hObject, handles);


% --- Executes on button press in checkbox_right.
function checkbox_right_Callback(hObject, eventdata, handles)
% hObject    handle to checkbox_right (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
new_value = get(hObject,'Value');
drawnow;     handles = guidata(hObject);
handles.parameters.General.RightOn = new_value; guidata(hObject, handles);
if new_value==0
    set(handles.slider_sensitivity_right,'Enable','off');
    set(handles.slider_gain_right,'Enable','off');
    set(handles.slider_volume_right,'Enable','off');
    set(handles.text_sens_right, 'Enable','off');
    set(handles.text_gain_right, 'Enable','off');
    set(handles.text_vol_right, 'Enable','off');
else
    set(handles.slider_sensitivity_right,'Enable','on');
    set(handles.slider_gain_right,'Enable','on');
    set(handles.slider_volume_right,'Enable','on');
    set(handles.text_sens_right, 'Enable','on');
    set(handles.text_gain_right, 'Enable','on');
    set(handles.text_vol_right, 'Enable','on');
end
% Hint: get(hObject,'Value') returns toggle state of checkbox_right


% --- Executes during object creation, after setting all properties.
function checkbox_right_CreateFcn(hObject, eventdata, handles)
% hObject    handle to checkbox_right (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called
handles.parameters.General.RightOn = 1; guidata(hObject, handles);

% --- Executes on slider movement.
function slider_sensitivity_left_Callback(hObject, eventdata, handles)
% hObject    handle to slider_sensitivity_left (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
new_value = get(hObject,'Value');
drawnow;     handles = guidata(hObject);
handles.parameters.Left.Sensitivity = new_value;
handles.parameters.Left.scale_factor =handles.parameters.Left.Sensitivity/32768;
guidata(hObject, handles);
set(handles.text_sens_left, 'String', ['Sensitivity = ' num2str(handles.parameters.Left.Sensitivity)]);
% Hints: get(hObject,'Value') returns position of slider
%        get(hObject,'Min') and get(hObject,'Max') to determine range of slider


% --- Executes during object creation, after setting all properties.
function slider_sensitivity_left_CreateFcn(hObject, eventdata, handles)
% hObject    handle to slider_sensitivity_left (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: slider controls usually have a light gray background.
if isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor',[.9 .9 .9]);
end

% --- Executes on slider movement.
function slider_sensitivity_right_Callback(hObject, eventdata, handles)
% hObject    handle to slider_sensitivity_right (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
new_value = get(hObject,'Value');
drawnow;     handles = guidata(hObject);
handles.parameters.Right.Sensitivity = new_value;
handles.parameters.Right.scale_factor =handles.parameters.Right.Sensitivity/32768;
set(handles.text_sens_right, 'String', ['Sensitivity = ' num2str(handles.parameters.Right.Sensitivity)]);
guidata(hObject, handles);
% Hints: get(hObject,'Value') returns position of slider
%        get(hObject,'Min') and get(hObject,'Max') to determine range of slider


% --- Executes during object creation, after setting all properties.
function slider_sensitivity_right_CreateFcn(hObject, eventdata, handles)
% hObject    handle to slider_sensitivity_right (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: slider controls usually have a light gray background.
if isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor',[.9 .9 .9]);
end

% --- Executes on slider movement.
function slider_gain_left_Callback(hObject, eventdata, handles)
% hObject    handle to slider_gain_left (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
new_value = get(hObject,'Value');
drawnow;     handles = guidata(hObject);
handles.parameters.Left.Gain = new_value;
handles.parameters.Left.gains_dB = handles.parameters.Left.Gain + handles.parameters.Left.BandGains;
handles.parameters.Left.gains = 10 .^ (handles.parameters.Left.gains_dB / 20.0);  %handles.parameters.Left.gains(23-handles.parameters.left.off_electrodes)=[];
set(handles.text_gain_left, 'String', ['Gain = ' num2str(handles.parameters.Left.Gain) 'dB']);
guidata(hObject, handles);
% Hints: get(hObject,'Value') returns position of slider
%        get(hObject,'Min') and get(hObject,'Max') to determine range of slider


% --- Executes during object creation, after setting all properties.
function slider_gain_left_CreateFcn(hObject, eventdata, handles)
% hObject    handle to slider_gain_left (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: slider controls usually have a light gray background.
if isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor',[.9 .9 .9]);
end

% --- Executes on slider movement.
function slider_gain_right_Callback(hObject, eventdata, handles)
% hObject    handle to slider_gain_right (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
new_value = get(hObject,'Value');
drawnow;     handles = guidata(hObject);
handles.parameters.Right.Gain = new_value;
handles.parameters.Right.gains_dB = handles.parameters.Right.Gain + handles.parameters.Right.BandGains;
handles.parameters.Right.gains = 10 .^ (handles.parameters.Right.gains_dB / 20.0);  %handles.parameters.right.gains(23-handles.parameters.right.off_electrodes)=[];
set(handles.text_gain_right, 'String', ['Gain = ' num2str(handles.parameters.Right.Gain) 'dB']);
guidata(hObject, handles);
% Hints: get(hObject,'Value') returns position of slider
%        get(hObject,'Min') and get(hObject,'Max') to determine range of slider


% --- Executes during object creation, after setting all properties.
function slider_gain_right_CreateFcn(hObject, eventdata, handles)
% hObject    handle to slider_gain_right (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: slider controls usually have a light gray background.
if isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor',[.9 .9 .9]);
end


% --- Executes on slider movement.
function slider_volume_left_Callback(hObject, eventdata, handles)
% hObject    handle to slider_volume_left (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
new_value = get(hObject,'Value');
drawnow;     handles = guidata(hObject);
handles.parameters.Left.Volume = new_value;
handles.parameters.Left.volume_level = handles.parameters.Left.Volume/10;  guidata(hObject, handles);
set(handles.text_vol_left, 'String', ['Volume = ' num2str(handles.parameters.Left.Volume)]);
% Hints: get(hObject,'Value') returns position of slider
%        get(hObject,'Min') and get(hObject,'Max') to determine range of slider


% --- Executes during object creation, after setting all properties.
function slider_volume_left_CreateFcn(hObject, eventdata, handles)
% hObject    handle to slider_volume_left (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: slider controls usually have a light gray background.
if isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor',[.9 .9 .9]);
end


% --- Executes on slider movement.
function slider_volume_right_Callback(hObject, eventdata, handles)
% hObject    handle to slider_volume_right (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
new_value = get(hObject,'Value');
drawnow;     handles = guidata(hObject);
handles.parameters.Right.Volume = new_value;
handles.parameters.Right.volume_level = handles.parameters.Right.Volume/10;guidata(hObject, handles);
set(handles.text_vol_right, 'String', ['Volume = ' num2str(handles.parameters.Right.Volume)]);
% Hints: get(hObject,'Value') returns position of slider
%        get(hObject,'Min') and get(hObject,'Max') to determine range of slider


% --- Executes during object creation, after setting all properties.
function slider_volume_right_CreateFcn(hObject, eventdata, handles)
% hObject    handle to slider_volume_right (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: slider controls usually have a light gray background.
if isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor',[.9 .9 .9]);
end



% % --- Executes during object creation, after setting all properties.
% function axes1_CreateFcn(hObject, eventdata, handles)
% % hObject    handle to axes1 (see GCBO)
% % eventdata  reserved - to be defined in a future version of MATLAB
% % handles    empty - handles not created until after all CreateFcns called
%
% % Hint: place code in OpeningFcn to populate axes1
% axes(hObject)
% imshow('processing_image.jpg');


% --- Executes on button press in button_stop.
function button_stop_Callback(hObject, eventdata, handles)
% hObject    handle to button_stop (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
handles.stop = 1;
% Update handles structure
guidata(hObject, handles);

% --- Executes on button press in button_start.
function button_start_Callback(hObject, eventdata, handles)
% hObject    handle to button_start (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Initialize
p = handles.parameters;
s = initializeBoard(p);

if (isfield(p,'Left') ==1)
left_comments = sprintf('Implant type = Nucleus CI24RE\nStimulation Rate = %spps\nPulse Width = %sus',num2str(p.Left.StimulationRate),num2str(p.Left.PulseWidth));
set(handles.text_left, 'String', left_comments);
end
if (isfield(p,'Right') ==1)
right_comments = sprintf('Implant type = Nucleus CI24RE\nStimulation Rate = %spps\nPulse Width = %sus',num2str(p.Right.StimulationRate),num2str(p.Right.PulseWidth));
set(handles.text_right, 'String', right_comments);
end



dummy_output_buffer = UART_start_buffer;

if (isfield(p,'Left') ==1)
    bufferHistory_left = (zeros(1, p.Left.block_size - p.Left.block_shift));
end

if (isfield(p,'Right') ==1)
    bufferHistory_right = (zeros(1, p.Right.block_size - p.Right.block_shift));
end

handles.stop = 0; guidata(hObject, handles);
hAx = handles.axes2;
maxA = 255;  minA = 0; 
xlabel('Electrodes'); 
elecbar = zeros(1,22); 
xlhand = get(hAx,'xlabel'); set(xlhand,'fontsize',5);

hAy = handles.axes3; axes(handles.axes3);
maxA2 = 0.2;  minA2 = -0.2;
index = 1; sigplot = zeros(1,16000); nSamples = numel(sigplot); %round(fs*timeBase);
hLine = plot(hAy,(1:nSamples)/16000,sigplot(:,index:index+nSamples-1));
xlabel('time');
ylim([minA2 maxA2]);
xlim([0 nSamples/16000]);

while handles.stop==0 % use while else timing won't be right
    drawnow;     handles = guidata(hObject); p = handles.parameters;
    if Wait(s)>= 512
        AD_data_bytes = Read(s, 512);
        AD_data=typecast(int8(AD_data_bytes), 'int16');    
        %tic;
        if (p.General.LeftOn == 1)
            audio_left = double(AD_data(1:2:end));          % Type cast to double for processing
            audio_left =  (p.Left.scale_factor).*audio_left; %2.3/32768 at 25 dB gain or 1/32768 at 33dB gain for 1kHz at 65dB SPl to equate to MCL p.sensitivity.
            stimuli.left = ACE_Processing_Realtime(audio_left, bufferHistory_left, p.Left);
        end
        
        if (p.General.RightOn == 1)
            audio_right=double(AD_data(2:2:end));
            audio_right = (p.Right.scale_factor).*audio_right;
            stimuli.right = ACE_Processing_Realtime(audio_right, bufferHistory_right, p.Right);
        end
        
        axes(handles.axes3);
        sigplot(nSamples-127:end) = audio_left;
        set(hLine,'ydata',sigplot); %(:,index:index+128-1));
        %drawnow  %updates the display
        sigplot(1:nSamples-128) = sigplot(129:nSamples);
        set(hAy,'Color',[0.1 0 0.1]);
                
        axes(handles.axes2);
        elecbar = zeros(1,22);
        cc = stimuli.left.current_levels(1:8); ee = stimuli.left.electrodes(1:8);
        elecbar(ee) = cc;
        bgraph = bar(elecbar(22:-1:1)); 
        set(bgraph,'FaceColor', [0.4 0 0.4]);
        ylim([minA maxA]); xlim([1 22]); 
        xlhand = get(hAx,'xlabel'); set(xlhand,'fontsize',1);
        hAx.XTick=1:1:22; hAx.XTickLabel =(22:-1:1);
        set(hAx,'Color',[0.1 0 0.1]);
        
        drawnow;
        Write(s, dummy_output_buffer,numel(dummy_output_buffer));
        clear AD_data_bytes; clear AD_data;
    end
    
end
delete(s); clear s;
