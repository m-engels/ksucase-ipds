function [Net,P_dim,Q_dim,P_gen]=Aggregate(iter)

if iter==1
    ieee37_temp=PowerSystemDataOPF();
    
    Net=Network_3Level(ieee37_temp);

    for i=2:37+1
        if ~isempty(Net.SubNode(i).Feeder)
            for j=1:size(Net.SubNode(i).Feeder,1)
                Net.SubNode(i).Feeder(j,7:12)=sum(Net.SubNode(i).FeederNode(j).Neighborhood(:,7:12));
                Net.SubNode(i).Feeder(j,14:16)=sum(Net.SubNode(i).FeederNode(j).Neighborhood(:,14:16));
            end
        end
    end

    for i=2:37+1
        if isempty(Net.SubNode(i).Feeder)
        else
            if i==31
                Net.Sub(i-1,7:12)=Net.SubNode(i).Feeder(:,7:12);
                Net.Sub(i-1,14:16)=Net.SubNode(i).Feeder(:,14:16);
            else
                Net.Sub(i-1,7:12)=sum(Net.SubNode(i).Feeder(:,7:12));
                Net.Sub(i-1,14:16)=sum(Net.SubNode(i).Feeder(:,14:16));
            end
        end
    end
    else
end

ieee37=Net.Sub;
% ieee37(:,7:12)=ieee37(:,7:12)/1000;% kW to MW
% ieee37(:,14:16)=ieee37(:,14:16)/1000;
connection=ieee37(:,3:4);
% pflow(:,3:5)=pflow(:,3:5)/1000;
% qflow(:,3:5)=qflow(:,3:5)/1000;
%% system inputs
Num_bus=max(max(ieee37(:,3:4)));% number of buses 
Num_branch=Num_bus-1;% number of branches
%% Branch data
%% active power schedule and power demand for all buses
P_dim(:,1:3) = [ieee37(:,7) ieee37(:,9) ieee37(:,11)];%/baseMVA; % known as Pl
P_dim=[0 0 0 ;P_dim]; 
Q_dim(:,1:3) = [ieee37(:,8) ieee37(:,10) ieee37(:,12)];%/baseMVA;  % known as Ql
Q_dim=[0 0 0 ;Q_dim]; 
% pflow(:,3:5)=pflow(:,3:5)/baseMVA;
% qflow(:,3:5)=qflow(:,3:5)/baseMVA;
%%

P_gen(:,1:3) = ieee37(:,14:16);%/baseMVA; % known as Pg
P_gen=[0 0 0 ;P_gen];

end