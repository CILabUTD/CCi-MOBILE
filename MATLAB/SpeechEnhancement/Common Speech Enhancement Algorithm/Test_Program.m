
XX=y{1};
Noisy_signal=awgn(XX,5,'measured')
cd ('C:\Users\nxm175730\Documents\Mamun\LAB\Research\Different Previous Work\Loizou_book_work\MATLAB_code\Chap 6 and 7 (statistical_based)')

output=wiener_iter(Noisy_signal,44100,1);%Iterative Wiener algorithm based on all-pole speech production model
cd ('C:\Users\nxm175730\Documents\Mamun\LAB\Program Matlab\Speech Analysis\Basic Codes')
figure
myspectrogram(Noisy_signal)








% clc
clear all;
tic;
FILENAME_noisy = ('C:\Users\nxm175730\Documents\Mamun\LAB\Research\Databases\Car_Noise_Data\timit_car00002_n5_data');
FILENAME_clean=('C:\Users\nxm175730\Documents\Mamun\LAB\Research\Databases\Car_Noise_Data\timitdata\fadg0')
cd ('C:\Users\nxm175730\Documents\Mamun\LAB\Program Matlab\Speech Analysis\Basic Codes')
[Noisy_signal] = load_speech_from_folder_Mamun(FILENAME_noisy);
[Clean_signal] = load_speech_from_folder_Mamun(FILENAME_clean);
Fs=8000;
cd ('C:\Users\nxm175730\Documents\Mamun\LAB\Research\Different Previous Work\Loizou_book_work\MATLAB_code\Chap 6 and 7 (statistical_based)')

for i=1:length(Noisy_signal{:})
    output{i}=wiener_iter(Noisy_signal{1,1}{i,1},Fs,3);%Iterative Wiener algorithm based on all-pole speech production model
    output1{i}=wiener_as(Noisy_signal{1,1}{i,1},Fs);% Wie  ner algorithm based on a priori SNR estimation
    output2{i}=wiener_wt(Noisy_signal{1,1}{i,1},Fs);% Wiener algorithm based on wavelet thresholding multi-taper spectra
end
load('C:\Users\nxm175730\Documents\Mamun\LAB\Program Matlab\Speech Enhancement\Environment Based Enhancement\p')
for i=1:length(Noisy_signal{:})
    cd ('C:\Users\nxm175730\Documents\Mamun\LAB\Program Matlab\Speech Enhancement\Environment Based Enhancement')
    Processed_Clean_Signal{i} = ACE_Process_CI((output{i})',p,Fs);
    cd ('C:\Users\nxm175730\Documents\Mamun\LAB\Program Matlab\Speech Enhancement\Environment Based Enhancement')
    Processed_Clean_Signal1{i} = ACE_Process_CI((output1{i})',p,Fs);
    cd ('C:\Users\nxm175730\Documents\Mamun\LAB\Program Matlab\Speech Enhancement\Environment Based Enhancement')
    Processed_Clean_Signal2{i} = ACE_Process_CI((output2{i})',p,Fs);
    
end
toc;