function [wave_files,labels]=load_speech_files_Mamun(folder)
%folder='D:\Philipos\database\NOIZEUS\airport\0dB';
filelist = dir([folder,'\','*.wav']);

%filelist=dir('*.wav');
for i=1:numel(filelist)
    file_path=[folder,'\',filelist(i).name];
    [data,Fs]=audioread(file_path);
    wave_files{i,1}=data;
    labels{i,1}=file_path;
end
end
