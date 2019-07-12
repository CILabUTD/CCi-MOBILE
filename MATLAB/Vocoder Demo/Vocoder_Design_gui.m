 function varargout = Vocoder_Design_gui(varargin)
% THE CCI-MOBILE VOCODER
%  -------------------------------------------------------------------------
% USAGE: 
%  Vocoder_Design_gui(varargin)
% -----------------------------------------------------------------------
% INPUTS: -------------------------------------------------------------------------
% Audio Signal (.wav file)

% OUTPUTS: -------------------------------------------------------------------------
% Vocoded signal
  
% FILE ASSOCIATIONS

% REFERENCES:
% 
% [1] . Ali, N. Mamun, A. Bruggeman, R. C. M. Chandra Shekar, 
%       J. N.Saba, and J. H. Hansen,“The cci-mobile vocoder,
%       ”The 176th Meeting of Acoustical Society of America, vol. 144,
%       no. 3, pp. 1872–1872,2018..
%
% [2]   Loizou, Philipos C. "Speech processing in vocoder-centric cochlear implants.
%       " Cochlear and brainstem implants. Vol. 64. Karger Publishers, 2006. 109-143. 
%      
% -------------------------------------------------------------------------
%
% Author(s): Nursadul Mamun, A. Bruggeman,J. H. Hansen 
% Date     : June 30, 2019
% Contact  : (nursadul.mamun,avamarie.brueggeman, john.hansen)@utdallas.edu
% -------------------------------------------------------------------------


% Begin initialization code - DO NOT EDIT
gui_Singleton = 1;
gui_State = struct('gui_Name',       mfilename, ...
    'gui_Singleton',  gui_Singleton, ...
    'gui_OpeningFcn', @Vocoder_Design_guiOpeningFcn, ...
    'gui_OutputFcn',  @Vocoder_Design_guiOutputFcn, ...
    'gui_LayoutFcn',  [], ...
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


% --- Executes just before Vocoder is made visible.
function Vocoder_Design_guiOpeningFcn(hObject, eventdata, handles, varargin)
% This function has no output args, see OutputFcn.
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% varargin   unrecognized PropertyName/PropertyValue pairs from the
%            command line (see VARARGIN)

% Choose default command line output for Vocoder
handles.output = hObject;

% Update handles structure
guidata(hObject, handles);
axes(handles.FigureHeader)
imshow('Header.png')

% UIWAIT makes Vocoder wait for user response (see UIRESUME)
% uiwait(handles.figurefilterbanks);


% --- Outputs from this function are returned to the command line.
function varargout = Vocoder_Design_guiOutputFcn(hObject, eventdata, handles)
% varargout  cell array for returning output args (see VARARGOUT);
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Get default command line output from handles structure
varargout{1} = handles.output;


% --- Executes during object creation, after setting all properties.
function FigureFilterBanks_CreateFcn(hObject, eventdata, handles)
% hObject    handle to FigureFilterBanks (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: place code in OpeningFcn to populate FigureFilterBanks



function edit1_Callback(hObject, eventdata, handles)
% hObject    handle to edit1 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)



% Hints: get(hObject,'String') returns contents of edit1 as text
%        str2double(get(hObject,'String')) returns contents of edit1 as a double


% --- Executes during object creation, after setting all properties.
function edit1_CreateFcn(hObject, eventdata, handles)
% hObject    handle to edit1 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end


% --- Executes on selection change in filterbank_number.
function filterbank_number_Callback(hObject, eventdata, handles)
% hObject    handle to filterbank_number (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
handles.filterbank_number=filterbank_number;
guidata(hObject,handles);




% Hints: contents = cellstr(get(hObject,'String')) returns filterbank_number contents as cell array
%        contents{get(hObject,'Value')} returns selected item from filterbank_number


% --- Executes during object creation, after setting all properties.
function filterbank_number_CreateFcn(hObject, eventdata, handles)
% hObject    handle to filterbank_number (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: popupmenu controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end


% --- Executes on button press in ButtonPlayOriginal.
function ButtonPlayOriginal_Callback(hObject, eventdata, handles)
% hObject    handle to ButtonPlayOriginal (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
signal=handles.signal;
Fs=handles.Fs;
sound(signal,Fs)


% --- Executes on button press in ButtonPlayVocoded.
function ButtonPlayVocoded_Callback(hObject, eventdata, handles)
% hObject    handle to ButtonPlayVocoded (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
Vocoded_Signal=handles.Vocoded_Signal;
Fs=handles.Fs;
sound(Vocoded_Signal,Fs)


% --- Executes on button press in ButtonUpdate.
function ButtonUpdate_Callback(hObject, eventdata, handles)
% hObject    handle to ButtonUpdate (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)


%%%%%%%%%%%%%%%% Get the Input signal From the handles %%%%%%%%%%
signal = handles.signal;
Fs = handles.Fs;
t = (1:numel(signal))./Fs;
%%%%%%%%%%%%%% Number of Filter bank Selection %%%%%%
Selection = get(handles.panel1,'Selectedobject');
Num_filter_bank = get(Selection,'string');
Num_filter_bank = str2num(Num_filter_bank);

%%%%%%%%%%%%%% Filter Type %%%%%%
Selection = get(handles.panel2,'Selectedobject');
Filter_type = get(Selection,'string');

%%%%%%%%%%%%%% Number of Filter bank Selection %%%%%%
Selection = get(handles.panel3,'Selectedobject');
Method = get(Selection,'string');

%%%%%%%%%%%%%%  Original program %%%%%%%%%%%

% wn=[200 272 359 464 591 745 931 1155 1426 1753 2149 2627 3205 3904 4748 5768 7000];
% xx=1:16/Num_filter_bank:length(wn);
% wn=wn(xx);

low_thresh = 400;
high_thresh = 7000;

[low_cutoffs,center_freqs,high_cutoffs] = mel(Num_filter_bank,low_thresh,high_thresh);	



%%%%%%%%%%%%%  Plotting Original Signal and its Spectrum %%%%%%%
axes(handles.FigureOriginalTop)
signal_axis = linspace(0, length(signal)/Fs, length(signal));
plot(signal_axis, signal);
title('Original Signal')
xlabel('Time (s)')
ylabel('Amplitude')

% figure
% plot(signal_axis, signal);
% savefig('Original_Signal_3.fig')
% close

axes(handles.FigureOriginalBottom)
spectrogram(signal,300,[],[],Fs,'yaxis')
title('Spectrogram of Original Signal')
%myspectrogram(signal)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  Analyzer %%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%  FIR FILTER %%%%%%%%%%%%%
for i = 1:Num_filter_bank
    if Filter_type == 'FIR'
        b(i,:) = fir1(48 ,[low_cutoffs(i) high_cutoffs(i)]/(Fs/2),'bandpass');
        Filt_clean(i,:) = filter( b(i,:),1,signal);
        [h(:,i), w(:,i)] = freqz( b(i,:),1);
                
        %%%%%%%%%%%%%%%%%%%%%% BUTTER FILTER %%%%%%%%%%%%%%
    elseif Filter_type == 'IIR'
        [b(i,:), a(i,:)] = butter(4,[low_cutoffs(i) high_cutoffs(i)]/(Fs/2),'bandpass');
        % [b,a] = cheby1(3,5,[(wn(i)/(fs/2))  (wn(i+1)/(fs/2))],'bandpass');
        Filt_clean(i,:) = filter(b(i,:),a(i,:),signal);
        [h(:,i), w(:,i)] = freqz(b(i,:),a(i,:));
        
    end
end
axes(handles.FigureFilterBanks)

freqaxis = linspace(low_cutoffs(1),high_cutoffs(end),length(h))';
xaxisfreq = repmat(freqaxis,1,Num_filter_bank);

plot(xaxisfreq,abs(h))
xlim([freqaxis(1) freqaxis(end)])

%plot(w,abs(h))
title('Audio Filter Banks')
xlabel('Frequency (Hz)')
ylabel('Amplitude')


%%%%%%%%%%%%%%%%%%%%%%%  Synthesizer %%%%%%%%%%%%%%%%%%%%%%%
%  noise = randi([-10 10],[1,numel(signal)]); noise=noise./10; % white noise for noiseband vocoder
noise = wgn(length(signal),1,10);
for chan_no = 1:Num_filter_bank
    if strcmp(Method,'Noise band') %% synthesis method (Noise_band)
        Noisy_ENV(chan_no,:) = abs(hilbert(Filt_clean(chan_no,:))).*noise'; % ENV detection and make noisy signal
    elseif strcmp(Method,'Sine wave') %% synthesis method (sine waves)
        Noisy_ENV(chan_no,:) = abs(hilbert(Filt_clean(chan_no,:))).*sin(2.*pi.*center_freqs(chan_no).*t);
    end
    
    if Filter_type == 'FIR'
        Vocoded_Signal(chan_no,:) = filter(b(chan_no,:),1, Noisy_ENV(chan_no,:));
    elseif Filter_type == 'IIR'
        Vocoded_Signal(chan_no,:) = filter(b(chan_no,:),a(chan_no,:), Noisy_ENV(chan_no,:));
    end
end
Vocoded_Signal = sum(Vocoded_Signal);

%%%%%%%%%%%%%  Plotting Vocoded Signal and its Spectrum %%%%%%%
Vocoded_Signal = Vocoded_Signal/max(abs(Vocoded_Signal)); % normalization

handles.Vocoded_Signal = Vocoded_Signal;
axes(handles.FigureVocodedTop)
plot(signal_axis, Vocoded_Signal);
title('Vocoded Signal')
xlabel('Time (s)')
ylabel('Amplitude')


axes(handles.FigureVocodedBottom)
spectrogram(Vocoded_Signal,300,[],[],Fs,'yaxis')



% % To save the spectrogram as a separate figure
% figure
% spectrogram(Vocoded_Signal,300,[],[],Fs,'yaxis');
% %s = gray; %s = flipud(s); %colormap(s)
% savefig('Vocoded_signal_spectrogram_3.fig')
% close

title('Spectrogram of Vocoded Signal')
%myspectrogram(Vocoded_Signal)
guidata(hObject,handles);

% Save Vocoded audio as .wav file
audiowrite('Vocoded_signal.wav',Vocoded_Signal,Fs);

% --- Executes on selection change in popupmenu4.
function popupmenu4_Callback(hObject, eventdata, handles)
% hObject    handle to popupmenu4 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: contents = cellstr(get(hObject,'String')) returns popupmenu4 contents as cell array
%        contents{get(hObject,'Value')} returns selected item from popupmenu4


% --- Executes during object creation, after setting all properties.
function popupmenu4_CreateFcn(hObject, eventdata, handles)
% hObject    handle to popupmenu4 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: popupmenu controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end



function edit2_Callback(hObject, eventdata, handles)
% hObject    handle to edit2 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of edit2 as text
%        str2double(get(hObject,'String')) returns contents of edit2 as a double


% --- Executes during object creation, after setting all properties.
function edit2_CreateFcn(hObject, eventdata, handles)
% hObject    handle to edit2 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end


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
%  set(handles.edit2,'string',Fs);



function edit3_Callback(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of edit3 as text
%        str2double(get(hObject,'String')) returns contents of edit3 as a double


% --- Executes during object creation, after setting all properties.
function edit3_CreateFcn(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end


% --- Executes on key press with focus on filterbank_number and none of its controls.
function filterbank_number_KeyPressFcn(hObject, eventdata, handles)
% hObject    handle to filterbank_number (see GCBO)
% eventdata  structure with the following fields (see MATLAB.UI.CONTROL.UICONTROL)
%	Key: name of the key that was pressed, in lower case
%	Character: character interpretation of the key(s) that was pressed
%	Modifier: name(s) of the modifier key(s) (i.e., control, shift) pressed
% handles    structure with handles and user data (see GUIDATA)


% --- If Enable == 'on', executes on mouse press in 5 pixel border.
% --- Otherwise, executes on mouse press in 5 pixel border or over filterbank_number.
function filterbank_number_ButtonDownFcn(hObject, eventdata, handles)
% hObject    handle to filterbank_number (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)


% --- Executes on button press in radiobutton2.
function radiobutton2_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton2 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton2


% --- Executes on button press in radiobutton3.
function radiobutton3_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton3


% --- Executes on button press in Filter_30.
function Filter_30_Callback(hObject, eventdata, handles)
% hObject    handle to Filter_30 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of Filter_30


% --- Executes during object creation, after setting all properties.
function FigureVocodedBottom_CreateFcn(hObject, eventdata, handles)
% hObject    handle to FigureVocodedBottom (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: place code in OpeningFcn to populate FigureVocodedBottom


% --- Executes during object creation, after setting all properties.
function FigureHeader_CreateFcn(hObject, eventdata, handles)
% hObject    handle to FigureHeader (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: place code in OpeningFcn to populate FigureHeader


% --- Executes on key press with focus on ButtonLoadAudio and none of its controls.
function ButtonLoadAudio_KeyPressFcn(hObject, eventdata, handles)
% hObject    handle to ButtonLoadAudio (see GCBO)
% eventdata  structure with the following fields (see MATLAB.UI.CONTROL.UICONTROL)
%	Key: name of the key that was pressed, in lower case
%	Character: character interpretation of the key(s) that was pressed
%	Modifier: name(s) of the modifier key(s) (i.e., control, shift) pressed
% handles    structure with handles and user data (see GUIDATA)


% --- Executes when selected object is changed in panel1.
function panel1_SelectionChangedFcn(hObject, eventdata, handles)
% hObject    handle to the selected object in panel1 
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)


% --- Executes on button press in radiobutton5.
function radiobutton5_Callback(hObject, eventdata, handles)
% hObject    handle to radiobutton5 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of radiobutton5


% --- Executes during object creation, after setting all properties.
function panel1_CreateFcn(hObject, eventdata, handles)
% hObject    handle to panel1 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called
