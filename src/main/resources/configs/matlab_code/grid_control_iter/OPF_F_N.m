function [Voltage,Angle,Qinjection,Pt,Qt,Pr,Qr,deltaP_s2f,deltaQ_s2f, Max_Feeder]=...
    OPF_F_N(ieee37_bus_data1,PPt,QQt,PPr,QQr,VVoltage,AAngle,Phase)

global Max_Feeder_Iter 
% i=19
% LABC=[7;8]
% GABC=14
GABC=Phase+13;
if GABC==14
    LABC=[7;8];
elseif GABC==15
    LABC=[9;10];
elseif GABC==16
    LABC=[11;12];
end
PF_PV=tan(acos(0.8));
baseMVA=2.5;
ft2mi=0.000189394;
Bus_ref_Sub = 1;      % reference bus 
Z=lineimpedance;  % Line impedance

PPr=PPr/(1000*baseMVA);QQr=QQr/(1000*baseMVA);
PPt=PPt/(1000*baseMVA);QQt=QQt/(1000*baseMVA);

ieee37_bus_data1(:,7:12)=ieee37_bus_data1(:,7:12)/1000;
ieee37_bus_data1(:,14:16)=ieee37_bus_data1(:,14:16)/1000;

Num_bus_Sub=size(ieee37_bus_data1,1)+1;% number of buses 
Num_branch_Sub=Num_bus_Sub-1;% number of branches
NodeNumber=ieee37_bus_data1(:,3:4);

ieee37_bus_data1(:,3:4)=ieee37_bus_data1(:,3:4)-ieee37_bus_data1(1,4)+2;
ieee37_bus_data1(1,3)=1;ieee37_bus_data1(1,4)=2;

epsilonP=PPt*0.2;
if epsilonP<0
    epsilonP=-epsilonP;
end
Limit_Slack_Sub = [PPt-epsilonP PPt+epsilonP];   % active power output limitation for slack generator (in per unit)

Pflow_s2f=PPt; 

Gen_bus_Sub=zeros(Num_bus_Sub,1);
Gen_bus_Sub(find(ieee37_bus_data1(:,GABC))+1)=1;% indicate on which buses there are generators
Gen_bus_Sub(1)=1;
Num_gen_Sub = sum(Gen_bus_Sub);        % number of generators

P_gen_Sub=[0; ieee37_bus_data1(:,GABC)]/baseMVA; 
%% active power schedule and power demand for all buses
P_dim_Sub = [0; ieee37_bus_data1(:,LABC(1))]/baseMVA;
Q_dim_Sub = [0; ieee37_bus_data1(:,LABC(2))]/baseMVA;
%% Q limit
Q_Lim=[-P_gen_Sub*PF_PV*0 P_gen_Sub*PF_PV];
%Q_Lim=[-P_gen_Sub*PF_PV P_gen_Sub*PF_PV]*0;

%Q_Lim=[Q_dim_Sub+P_dim_Sub-P_gen_Sub Q_dim_Sub+P_dim_Sub-P_gen_Sub]; 

%%
Q_Lim_Sub=Q_Lim(find(Gen_bus_Sub>0),:);

epsilonQ=QQt*0.2;

if epsilonQ<0
    epsilonQ=-epsilonQ;
end
Q_Lim_Sub(1,1)=QQt-epsilonQ;Q_Lim_Sub(1,2)=QQt+epsilonQ;

Qflow_s2f=QQt;


%% Ymatrix construction

for J=1:Num_branch_Sub
    Z_Sub=cell2mat(Z(ieee37_bus_data1(J,6)-720));
    YbusSub(ieee37_bus_data1(J,3),ieee37_bus_data1(J,4)) = -inv(ieee37_bus_data1(J,5)*ft2mi*Z_Sub(1,1));
    YbusSub(ieee37_bus_data1(J,4),ieee37_bus_data1(J,3)) = YbusSub(ieee37_bus_data1(J,3),ieee37_bus_data1(J,4));
end

for J=1:Num_bus_Sub
    YbusSub(J,J)=-sum(YbusSub(:,J));
end

Ybus_abs = abs(YbusSub);   % amplitude of Y-matrix
Ybus_ang = angle(YbusSub); % angle of Y-matrix

%% bus voltage limit
v=[0.95*ones(Num_bus_Sub,1) 1.05*ones(Num_bus_Sub,1)];
v(1,1)=VVoltage;v(1,2)=VVoltage;   
V_angle=AAngle;
N_Neighbor=size(ieee37_bus_data1,1);
PPPt=P_dim_Sub-P_gen_Sub;QQQt=Q_dim_Sub;
%% optimization
% options=optimset('Display','off','Algorithm','interior-point','TolCon',1e-3,'TolFun',1e-2,'TolX',1e-6);
options=optimset('Display','off','Algorithm','active-set','TolCon',1e-6,'TolFun',1e-6,'TolX',1e-6);

% [x,fval,exitflag] = fmincon(@(x)LossObjectiveFunctionSub(x,Num_bus_Sub),ones(1,3*Num_bus_Sub+1),[],[],[],[],[],[],@(x)LossConstraintSub1(x,Num_bus_Sub,Num_gen_Sub,Bus_ref_Sub,Limit_Slack_Sub,Gen_bus_Sub,Q_Lim_Sub,Q_dim_Sub,P_dim_Sub,P_gen_Sub,Ybus_abs,Ybus_ang,v,V_angle),options);
% [x,fval,exitflag] = fmincon(@(x)LossObjectiveFunctionSub(x,Num_gen_Sub,Pflow_s2f,Qflow_s2f),ones(1,2*Num_bus_Sub+Num_gen_Sub+1),[],[],[],[],[],[],@(x)LossConstraints1(x,Num_bus_Sub,Num_gen_Sub,Bus_ref_Sub,Limit_Slack_Sub,Gen_bus_Sub,Q_Lim_Sub,Q_dim_Sub,P_dim_Sub,P_gen_Sub,Ybus_abs,Ybus_ang,v,V_angle),options);

%[x,fval,exitflag,output] = fmincon(@(x)LossObjectiveFunctionSub(x,Num_gen_Sub,Pflow_s2f,Qflow_s2f,Net,i,GABC),ones(1,2*Num_bus_Sub+Num_gen_Sub+1),[],[],[],[],[],[],@(x)LossConstraints1(x,Num_bus_Sub,Num_gen_Sub,Bus_ref_Sub,Limit_Slack_Sub,Gen_bus_Sub,Q_Lim_Sub,Q_dim_Sub,P_dim_Sub,P_gen_Sub,Ybus_abs,Ybus_ang,v,V_angle),options);
[x,fval,exitflag,output] = fmincon(@(x)Obj2(x,Num_bus_Sub,Pflow_s2f,Qflow_s2f,PPPt,QQQt,PPr,QQr,Phase,N_Neighbor),ones(1,2*Num_bus_Sub+Num_gen_Sub+1),[],[],[],[],[],[],@(x)LossConstraints1(x,Num_bus_Sub,Num_gen_Sub,Bus_ref_Sub,Limit_Slack_Sub,Gen_bus_Sub,Q_Lim_Sub,Q_dim_Sub,P_dim_Sub,P_gen_Sub,Ybus_abs,Ybus_ang,v,V_angle),options);

FunctionValue = fval;
SystemOutput = x';

xx=zeros(1,length(Gen_bus_Sub));
xx(find(Gen_bus_Sub>0))=x(1:Num_gen_Sub);
xx=[xx x(Num_gen_Sub+1:2*Num_bus_Sub+Num_gen_Sub+1)];

if exitflag==-2
    disp('not converged')
end
Max_Feeder=max(Max_Feeder_Iter,output.iterations);
% Net.SubNode(i).Voltage(:,LABC'-6)=[x(Num_bus_Sub+1+1:2*Num_bus_Sub+1)' x(2*Num_bus_Sub+1+1:3*Num_bus_Sub+1)'];
% Net.SubNode(i).Feeder(:,GABC+3)=x(1+1:Num_bus_Sub)'*baseMVA*1000;
%Voltage(size([xx(Num_bus_Sub+1+1:2*Num_bus_Sub+1)' xx(2*Num_bus_Sub+1+1:3*Num_bus_Sub+1)'],1),1:6)=0;
Voltage=xx(Num_bus_Sub+1+1:2*Num_bus_Sub+1)'; 
Angle=xx(2*Num_bus_Sub+1+1:3*Num_bus_Sub+1)';
Qinjection=xx(1+1:Num_bus_Sub)'*baseMVA*1000;

%Net.SubNode(i).Pup(GABC-13)=(P_dim(i,GABC-13)-P_gen(i,GABC-13))*baseMVA*1000;
Pr=xx(Num_bus_Sub+1)*baseMVA*1000;

%Net.SubNode(i).Qup(GABC-13)=(Q_dim(i,GABC-13)-Qinjection(i,GABC-13))*baseMVA*1000;
Qr=xx(1)*baseMVA*1000;
Pt=(P_dim_Sub(2:end,:)-P_gen_Sub(2:end,:))*baseMVA*1000;
Qt=(Q_dim_Sub(2:end,:)-Qinjection)*baseMVA*1000;
deltaP_s2f=abs(Pr-PPt*baseMVA*1000);
deltaQ_s2f=abs(Qr-QQt*baseMVA*1000);
% end


