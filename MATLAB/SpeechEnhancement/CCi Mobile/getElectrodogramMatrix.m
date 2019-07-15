function [electrodogramMatrix] = getElectrodogramMatrix(electrodeVector,currentVector)
%GETELECTRODOGRAMMATRIX This function returns a matrix of electrodogram
%values. Developed for EE-6366, Fall 2018.
%   electrodeVector     input   column vector of electrode numbers
%   currentVector       input   column vector of current values
%   electrodogramMatrix output  electrodogram matrix

%% Initialize data
numElectrodes = 22;
halfElectrodes = numElectrodes/2;
electrodesPerCycle = 8;
numCycles = length(electrodeVector)/electrodesPerCycle;

electrodogramMatrix = zeros(numElectrodes,round(numCycles)); % initialize matrix with zeros
nonzeroIndices = find(currentVector); % indices of non-zero values
whichColumn = floor((nonzeroIndices-1)/8)+1; % which columns of electrodogramMatrix will be non-zero
uniqueColumns = unique(whichColumn); % remove duplicate non-zero column information

%% Fix electrode numbers
% Electrode values must be flipped (E1 should be E22, E2 should be E21, E3 should be E20, etc.)
for h=1:length(nonzeroIndices)
    currentElectrode = electrodeVector(nonzeroIndices(h));
    if currentElectrode <= halfElectrodes % if electrode is E1 to E11
        electrodeVector(nonzeroIndices(h)) = numElectrodes - abs(1-currentElectrode); % place in E12 to E22
    else % if electrode is E12 to E22
        electrodeVector(nonzeroIndices(h)) = 1 + (numElectrodes-currentElectrode); % place in E1 to E11
    end
end

%% Calculate matrix
nonzeroCounter = 1; % variable to progress through each nonzero value
for i=1:length(uniqueColumns) % for each non-zero column of electrodogramMatrix
    numActiveInColumn = sum(whichColumn == uniqueColumns(i)); % number of non-zero values in that column
    for j=1:numActiveInColumn
        electrodogramMatrix(electrodeVector(nonzeroIndices(nonzeroCounter)),uniqueColumns(i))...
            = currentVector(nonzeroIndices(nonzeroCounter));
        nonzeroCounter = nonzeroCounter + 1;
    end
end

%% Plots mesh of spectrogram (optional)
% figure
% mesh(electrodogramMatrix)
% xlabel('Time (ms)')
% ylabel('Electrode Number')
% zlabel('Current Level')
end

