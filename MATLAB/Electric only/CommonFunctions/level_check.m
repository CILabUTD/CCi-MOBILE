function [ mod_map ] = level_check( org_map )
%UNTITLED2 Summary of this function goes here
%   Detailed explanation goes here

mod_map = org_map;

if (isfield(org_map,'Left')==1)
    % check left parameters
    n = org_map.Left.NumberOfBands;
    thr = org_map.Left.THR; mcl = org_map.Left.MCL;
    level_check_flag1_l = 0; level_check_flag2_l = 0;
    
    for i=1:n
        if (thr(i))<0
            fprintf('Channel %d : THR(%d) = %d -> Corrected to 0.\n',i, i, thr(i));
            thr(i)=0;
        end
        
        if (mcl(i)>255)
            fprintf('Channel %d : MCL(%d) = %d -> Corrected to 255.\n',i, i, mcl(i));
            mcl(i)=255;
            level_check_flag1_l = 1;
        end
        
        if (thr(i)>mcl(i))
            fprintf('Channel %d : THR(%d) = %d is greater than MCL(%d) = %d -> Corrected to 0.\n',i, i, thr(i), i, mcl(i));
            thr(i)=0;
            level_check_flag2_l = 1;
        end
    end
    
    if (level_check_flag1_l ==1)
        errordlg('Maximum Comfort Level(s) greater than 255', 'Map Error');
    end
    
    if (level_check_flag2_l ==1)
        errordlg('Threshold Level(s) of left ear greater than Maximum Comfort Level(s)', 'Map Error');
    end
    
    mod_map.Left.THR=thr;
    mod_map.Left.MCL=mcl;
end

if (isfield(org_map,'Right')==1)
    % check right parameters
    n = org_map.Right.NumberOfBands;
    thr = org_map.Right.THR; mcl = org_map.Right.MCL;
    level_check_flag1_r = 0; level_check_flag2_r = 0;

    for i=1:n
        if (thr(i))<0
            fprintf('Channel %d : THR(%d) = %d -> Corrected to 0.\n',i, i, thr(i));
            thr(i)=0;
        end
        
        if (mcl(i)>255)
            fprintf('Channel %d : MCL(%d) = %d -> Corrected to 255.\n',i, i, mcl(i));
            mcl(i)=255;
            level_check_flag1_r = 1;
        end
        
        if (thr(i)>mcl(i))
            fprintf('Channel %d : THR(%d) = %d is greater than MCL(%d) = %d -> Corrected to 0.\n',i, i, thr(i), i, mcl(i));
            thr(i)=0;
            level_check_flag2_r = 1;
        end
    end
    
    if (level_check_flag1_r ==1)
        errordlg('Maximum Comfort Level(s) greater than 255', 'Map Error');
    end
    
    if (level_check_flag2_r ==1)
        errordlg('Threshold Level(s) of right ear greater than Maximum Comfort Level(s)', 'Map Error');
    end
    
    mod_map.Right.THR=thr;
    mod_map.Right.MCL=mcl;
    
end
end

