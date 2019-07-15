
function [Output_files]=load_speech_from_folder(start_path, jj)
% This function read the all .raw and .wav data from all subfolder in a folder.
%start_path: Path of the main folder
%jj=1 for .wav data and jj=2 for .raw data, jj=3 for .mat data and jj=4 for .sph data, 
%Output_files: Provides all  files in all subfolders
% Credited by: Nursadul Mamun,  Email: nursadul.mamun@utdallas.edu
% University of Texas at Dallas, USA


SubFolders = genpath(start_path);
listofFolder = {};j = 1; wave_files = {}; Output_files = {}; baseFileNames = {};
while true
    [singleSubFolder, SubFolders] = strtok(SubFolders, ';');
    if isempty(singleSubFolder)
        break;
    end
    listofFolder = [listofFolder singleSubFolder];
end


 %%%%%%%%%%%%%%%%%   For .wav data Extraction  %%%%%%%%%%

if jj == 1
    for k = 1 : length(listofFolder)
        path = listofFolder{k};
        fprintf('Processing folder %s\n', path);
        filePattern = sprintf('%s/*.wav', path);
        baseFileNames = dir(filePattern);
        
        %%%%%%%%%%%%%%%%%%%%  Read the .wav files %%%%%%%%%%%%%%%%%%
        for i=1:numel(baseFileNames)
            file_path=[path,'\',baseFileNames(i).name];
            data = audioread(file_path);
            wave_files{i,1}=data;
            labels{i,1}=file_path;
        end
        
        if ~isempty(baseFileNames)~=0
            Output_files{j}=wave_files;
            j=j+1;
        end
    end
end


 %%%%%%%%%%%%%%%%%   For .raw data Extraction  %%%%%%%%%%
if jj == 2
    for k = 1 : length(listofFolder)
        path = listofFolder{k};
        list = dir([path,'\','*.raw']);
        for i = 1 : length(list)
            filename=list(i).name;
            file_path = [path,'\',filename];
            file = fopen(file_path,'r');
            data = fread(file,'int16');
            fclose(file);
            data=double(data);
            data = mapstd(data',0,1);
            baseFileNames{i,1} = data;
        end
        if ~isempty(baseFileNames)
            Output_files{j} = baseFileNames;
            j=j+1;
        end
    end 
    
end
 %%%%%%%%%%%%%%%%%   For .mat data Extraction  %%%%%%%%%%

if jj == 3
    for k = 1 : length(listofFolder)
        path = listofFolder{k};
        fprintf('Processing folder %s\n', path);
        filePattern = sprintf('%s/*.mat', path);
        baseFileNames = dir(filePattern);
        
        %%%%%%%%%%%%%%%%%%%%  Read the .wav files %%%%%%%%%%%%%%%%%%
        for i=1:numel(baseFileNames)
            file_path=[path,'\',baseFileNames(i).name];
            data = audioread(file_path);
            wave_files{i,1}=data;
            labels{i,1}=file_path;
        end
        
        if ~isempty(baseFileNames)~=0
            Output_files{j}=wave_files;
            j=j+1;
        end
    end
end


 %%%%%%%%%%%%%%%%%   For .sph data Extraction  %%%%%%%%%%

if jj == 4
    for k = 1 : length(listofFolder)
        path = listofFolder{k};
        fprintf('Processing folder %s\n', path);
        filePattern = sprintf('%s/*.sph', path);
        baseFileNames = dir(filePattern);
        
        %%%%%%%%%%%%%%%%%%%%  Read the .sph files %%%%%%%%%%%%%%%%%%
        for i=1:numel(baseFileNames)
            file_path=[path,'\',baseFileNames(i).name];
            cd 'C:\Users\nxm175730\Documents\Mamun\LAB\Program Matlab\Toolbox\voicebox'

    [data,Fs]=readsph(file_path,'wt');
    wave_files{i,1}=data;
            labels{i,1}=file_path;
        end
        
        if ~isempty(baseFileNames)~=0
            Output_files{j}=wave_files;
            j=j+1;
        end
    end
end

end



