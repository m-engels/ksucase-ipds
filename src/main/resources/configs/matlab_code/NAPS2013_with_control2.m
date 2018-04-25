clc
close all
clear all

%% Initialization
P_Load_interval=[1.2 1.175 1.15 1.1...% shows hourly load variation
    1.075 1.125 1.225 1.4 1.225 1.2 1.2 1.3 1.4 1.5 1.6 1.7 1.8 2 2.3 2.5 2.7 2.6 2 1.5]/2.7;
[Load, Generation]=TwoHourLoad; % Load and generation profile
Generation=Generation/1000;PV_capacity=870/1000; % generation profile in MW

Z=lineimpedance;      
[Data]=ieee37_bus;
ieee37_bus_data=sortrows(Data,4);

candida_DG=zeros(1,size(Data,1)+1);
candida_home=zeros(1,size(Data,1)+1);

candida_DG(find(ieee37_bus_data(:,2)>700000)+2)=1;   % PV enabled homes
candida_home(find(ieee37_bus_data(:,2)>70000 & ieee37_bus_data(:,2)<700000)+1)=1; 

PF=tan(acos(0.8));  % generation power factor
PF_load=tan(acos(0.9)); % load power factor

%% Finalizing system data
ieee37_bus_data_temp=ieee37_bus_data;
ieee37_bus_data1=ieee37_bus_data;
ieee37_bus_data1(43:46,7)=Load(1,1:4)';
ieee37_bus_data1(43:46,7+1)=ieee37_bus_data1(43:46,7)*PF_load;
ieee37_bus_data1(48:51,7)=Load(1,5:8)';
ieee37_bus_data1(48:51,7+1)=ieee37_bus_data1(48:51,7)*PF_load;
ieee37_bus_data1(53:56,7)=Load(1,9:12)';
ieee37_bus_data1(53:56,7+1)=ieee37_bus_data1(53:56,7)*PF_load;
ieee37_bus_data1(58:61,7)=Load(1,13:16)';
ieee37_bus_data1(58:61,7+1)=ieee37_bus_data1(58:61,7)*PF_load;
ieee37_bus_data1(1:37,7:12)=ieee37_bus_data(1:37,7:12)*P_Load_interval(12);

[V1]=PowerFlowTest12(ieee37_bus_data1, Z); % V1 is system initial voltage at t=1 
%%
output=[[0 0 0 0 0 0 ;ieee37_bus_data1(:,7:12)] zeros(62,2) V1(:,2:4)];

Volt_AA=[];Volt_BB=[];Volt_CC=[];
% Volt_Aw=[];Volt_Bw=[];Volt_Cw=[];
Q_PV=[];delta_PL=[];delta_QL=[];Q_old=[];violation=0;
for i=1:3600
    if i/20 == floor(i/20)
        i
    end
    if i<=3600
        ieee37_bus_data_temp(1:37,7:12)=ieee37_bus_data(1:37,7:12)*P_Load_interval(12);
    else
        ieee37_bus_data_temp(1:37,7:12)=ieee37_bus_data(1:37,7:12)*P_Load_interval(13);
    end
    ieee37_bus_data_temp(43:46,7)=Load(i,1:4)';
    ieee37_bus_data_temp(43:46,7+1)=ieee37_bus_data_temp(43:46,7)*PF_load;
    ieee37_bus_data_temp(48:51,7)=Load(i,5:8)';
    ieee37_bus_data_temp(48:51,7+1)=ieee37_bus_data_temp(48:51,7)*PF_load;
    ieee37_bus_data_temp(53:56,7)=Load(i,9:12)';
    ieee37_bus_data_temp(53:56,7+1)=ieee37_bus_data_temp(53:56,7)*PF_load;
    ieee37_bus_data_temp(58:61,7)=Load(i,13:16)';
    ieee37_bus_data_temp(58:61,7+1)=ieee37_bus_data_temp(58:61,7)*PF_load;


    output=[output [0 0 0 0 0 0 ;ieee37_bus_data_temp(:,7:12)] candida_DG'.*repmat(Generation(i),size(Data,1)+1,1) zeros(62,1) zeros(62,3)];

    delta_PQ=output(44:62,(i-1)*7+(i-1)*4+1+11:i*7+(i-1)*4+11)-output(44:62,(i-1)*7+(i-1)*4+1:i*7+(i-1)*4);
    Q_new=delta_PQ(:,2)+delta_PQ(:,1)-delta_PQ(:,7)+output(44:62,(i-1)*11+8);
    Q_old=[Q_old output(44:62,(i-1)*11+8)];
   
    Qmax=Generation(i)*PF;
    if (Q_new(1) > Qmax)
        Q_new(1)=Qmax;violation=violation+1;
    elseif (Q_new(1) < -Qmax)
        Q_new(1)=-Qmax;violation=violation+1;
    end


    if (Q_new(6) > Qmax)
        Q_new(6)=Qmax;violation=violation+1;
    elseif (Q_new(6) < -Qmax)
        Q_new(6)=-Qmax;violation=violation+1;
    end

    if (Q_new(11) > Qmax)
        Q_new(11)=Qmax;violation=violation+1;
    elseif (Q_new(11) < -Qmax)
        Q_new(11)=-Qmax;violation=violation+1;
    end

    if (Q_new(16) > Qmax)
        Q_new(16)=Qmax;violation=violation+1;
    elseif (Q_new(16) < -Qmax)
        Q_new(16)=-Qmax;violation=violation+1;
    end

     Q_PV=[Q_PV Q_new];

    output(44:62,i*11+8)=Q_new;
    ieee37_bus_data_temp(43:61,7+1)=ieee37_bus_data_temp(43:61,7+1)-Q_new;
    ieee37_bus_data_temp(43,7)=ieee37_bus_data_temp(43,7)-Generation(i);
    ieee37_bus_data_temp(48,7)=ieee37_bus_data_temp(48,7)-Generation(i);
    ieee37_bus_data_temp(53,7)=ieee37_bus_data_temp(53,7)-Generation(i);
    ieee37_bus_data_temp(58,7)=ieee37_bus_data_temp(58,7)-Generation(i); 
    
    [Volt]=PowerFlowTest12(ieee37_bus_data_temp, Z);
    Volt_AA=[Volt_AA Volt(:,2)];Volt_BB=[Volt_BB Volt(:,3)];Volt_CC=[Volt_CC Volt(:,4)];
    output(1:62,i*11+9:(i+1)*11)= Volt(:,2:4);
end
%%

plot(Volt_AA(59,:),'r');
% plot(Volt_Aw(59,:),'b');
% hold on
% plot(Volt_A(59,:),'g');

% I_Volt_Aw=0;I_Volt_A=0;I_Volt_AA=0;
% for i= 2:3600
%     I_Volt_Aw=I_Volt_Aw+abs(Volt_Aw(59,i)-Volt_Aw(59,i-1));
%     I_Volt_A=I_Volt_A+abs(Volt_A(59,i)-Volt_A(59,i-1));
%     I_Volt_AA=I_Volt_AA+abs(Volt_AA(59,i)-Volt_AA(59,i-1));
% end



