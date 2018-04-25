%function [Voltage,Angle,Qinjection,Pt,Qt,deltaP_s2f,deltaQ_s2f]=OPF_Sub(ieee37,iter,Pr,Qr)
function [Voltage,Angle,Qinjection,Pt,Qt]=OPF_Sub(ieee37,iter,Pr,Qr)

global P_dim Q_dim P_gen Z;

P_dim=[];Q_dim=[];P_gen=[];Q_gen=[];
%%
Z=lineimpedance;  % Line impedance
Bus_ref = 1;      % reference bus 
baseMVA = 2.500000;% MVA base
vb = 4.800;%kV
zb = (vb^2)/baseMVA;
%Ib=vb/zb;
ft2mi=0.000189394;
%%
Pr=Pr/(1000*baseMVA);Qr=Qr/(1000*baseMVA);
ieee37(:,7:12)=ieee37(:,7:12)/1000;% kW to MW
ieee37(:,14:16)=ieee37(:,14:16)/1000;
connection=ieee37(:,3:4);
% pflow(:,3:5)=pflow(:,3:5)/1000;
% qflow(:,3:5)=qflow(:,3:5)/1000;
%% system inputs
Num_bus=max(max(ieee37(:,3:4)));% number of buses 
Num_branch=Num_bus-1;% number of branches
%% Branch data
%% active power schedule and power demand for all buses
P_dim(:,1:3) = [ieee37(:,7) ieee37(:,9) ieee37(:,11)]/baseMVA; % known as Pl
P_dim=[0 0 0 ;P_dim]; 
Q_dim(:,1:3) = [ieee37(:,8) ieee37(:,10) ieee37(:,12)]/baseMVA;  % known as Ql
Q_dim=[0 0 0 ;Q_dim]; 
% pflow(:,3:5)=pflow(:,3:5)/baseMVA;
% qflow(:,3:5)=qflow(:,3:5)/baseMVA;
%%
Limit_Slack = [-1 -1 -1 1 1 1];   % active power output limitation for slack generator (in per unit)

P_gen(:,1:3) = ieee37(:,14:16)/baseMVA; % known as Pg
P_gen=[0 0 0 ;P_gen];

%%
PF_PV=tan(acos(0.8));
%Q_Lim=[Q_dim+P_dim-P_gen Q_dim+P_dim-P_gen];
%Q_Lim=[-P_gen*PF_PV P_gen*PF_PV]*0;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%   THIS IS THE MAJOR CHANGE     %%%%%%%%%%%%%%%%%%%%%%%%

Q_Lim=[-P_gen*PF_PV*0 P_gen*PF_PV]; 

%%%%%%%%%%%%%%   THIS IS THE MAJOR CHANGE     %%%%%%%%%%%%%%%%%%%%%%%%
% Q_Lim(1,:)=[-0.01 -0.01 -0.01 0.007 0.007 0.007];
Q_Lim(1,:)=40*[-0.01 -0.01 -0.01 0.01 0.01 0.01];
% Q_Lim(1,:)=[-0.1 -0.1 -0.1 0.1 0.1 0.1];

%% Ymatrix construction
Ybus=cell(Num_bus,Num_bus);
k=1;
for J=1:Num_branch
    Ybus{ieee37(J,3),ieee37(J,4)} = -inv(ieee37(J,5)*ft2mi*cell2mat(Z(ieee37(J,6)-720))./zb);
    Ybus{ieee37(J,4),ieee37(J,3)} = Ybus{ieee37(J,3),ieee37(J,4)};
end

for J=1:Num_bus
    catA=cat(3,Ybus{:,J});
    Ybus{J,J}=-sum(catA,3);
end
%%
Pt=P_dim-P_gen;Qt=Q_dim;
%%
options = optimset('Display','off','MaxIter',1000000,'MaxFunEvals',inf,'Algorithm','interior-point','TolFun',1e-003,'TolCon',1e-005);   
%[x,fval,exitflag,output,lambda] = fmincon(@ThreePhaseObj,x,[],[],[],[],[],[],@ThreePhaseLossConstraint,options);
% [x,fval] = fmincon(@(x)ThreePhaseObj1(x,Num_bus),ones(9*Num_bus+3,1),[],[],[],[],[],[],@(x)ThreePhaseLossConstraintTest(x,Num_bus,Bus_ref,Limit_Slack,Q_Lim,connection),options);     

[x,fval,exitflag,output] = fmincon(@(x)Obj1(x,Num_bus,Pr,Pt,Qr,Qt,iter),ones(1,9*Num_bus+3),[],[],[],[],[],[],@(x)ThreePhaseLossConstraint3(x,Num_bus,Bus_ref,Limit_Slack,Q_Lim,Ybus,connection),options);     
%%
if exitflag==-2
    fprintf('Not converged')
end
Max_Sub_Iter(iter)=output.iterations;
FunctionValue(iter) = fval;
X1=x';
Voltage=[];Angle=[];Qinject=[];
for i=1:Num_bus
%     if i<=3*Num_bus
        Voltage=[Voltage;x(3*(i-1)+1:3*(i-1)+3)];
%     elseif i<=2*3*Num_bus
        Angle=[Angle;x(3*Num_bus+3*(i-1)+1:3*Num_bus+3*(i-1)+3)];
%     elseif i<=3*3*Num_bus
        Qinject=[Qinject;x(2*3*Num_bus+3*(i-1)+1:2*3*Num_bus+3*(i-1)+3)];
%     end
end

% deltaP_s2f=abs(Pr-Pt);
% deltaQ_s2f=abs(Qr-Qt);

Qinjection=Qinject*baseMVA*1000;
Pt=(P_dim-P_gen)*baseMVA*1000;Qt=(Q_dim-Qinject)*baseMVA*1000;

