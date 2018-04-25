function [Sensor_Data_full,Sensor_Data_partial]=get_Voltage(Sensor_Data_Denise,t)

%% System description
[Load, Generation]=TwoHourLoad; % Load and generation profile
Generation=Generation/1000;%PV_capacity=870/1000; % generation profile in MW
Z=lineimpedance;  % Line impedance
% [PA QA PB QB PC QC]=Load1;
P_Load_interval=[1.2 1.175 1.15 1.1...% shows hourly load variation
    1.075 1.125 1.225 1.4 1.225 1.2 1.2 1.3 1.4 1.5 1.6 1.7 1.8 2 2.3 2.5 2.7 2.6 2 1.5]/2.7;
[Data]=ieee37_bus;
ieee37_bus_data=sortrows(Data,4);

PF=tan(acos(0.8));  % generation power factor
PF_load=tan(acos(0.9)); % load power factor

%%
% Sensor_Data=[0 0 0 0 0 0;PA QA PB QB PC QC];
nNode=62;
PV_PQ=zeros(nNode,2);
PV_enabled=[44,49,54,59];


if t==1
    %% Full sensor data calculation    
    PV_PQ(PV_enabled,1)=Generation(t);
    ieee37_bus_data1=ieee37_bus_data;
    ieee37_bus_data1(1:37,7:12)=ieee37_bus_data(1:37,7:12)*P_Load_interval(12);


    ieee37_bus_data1(43:46,7)=Load(1,1:4)';
    ieee37_bus_data1(43:46,7+1)=ieee37_bus_data1(43:46,7)*PF_load;
    ieee37_bus_data1(48:51,7)=Load(1,5:8)';
    ieee37_bus_data1(48:51,7+1)=ieee37_bus_data1(48:51,7)*PF_load;
    ieee37_bus_data1(53:56,7)=Load(1,9:12)';
    ieee37_bus_data1(53:56,7+1)=ieee37_bus_data1(53:56,7)*PF_load;
    ieee37_bus_data1(58:61,7)=Load(1,13:16)';
    ieee37_bus_data1(58:61,7+1)=ieee37_bus_data1(58:61,7)*PF_load;

    Sensor_Data=[0 0 0 0 0 0; ieee37_bus_data1(:,7:12)];

    ieee37_bus_data1(43,7)=ieee37_bus_data1(43,7)-Generation(1);
    ieee37_bus_data1(48,7)=ieee37_bus_data1(48,7)-Generation(1);
    ieee37_bus_data1(53,7)=ieee37_bus_data1(53,7)-Generation(1);
    ieee37_bus_data1(58,7)=ieee37_bus_data1(58,7)-Generation(1); 

    [Volt]=PowerFlowTest12(ieee37_bus_data1, Z);

    Sensor_Data_full=[Sensor_Data PV_PQ Volt(:,2:4)];
    %% Partial sensor data calculation   
    PV_PQ(PV_enabled,1)=Generation(t+1);
    Sensor_Data_partial=[Sensor_Data PV_PQ zeros(nNode,3)];
else
     %% Full sensor data calculation     
    ieee37_bus_data1=ieee37_bus_data;
    ieee37_bus_data1(:,7:12)=Sensor_Data_Denise(2:nNode,1:6);
    
    Sensor_Data=[0 0 0 0 0 0; ieee37_bus_data1(:,7:12)];
    PV_PQ(PV_enabled,1)=Sensor_Data_Denise(PV_enabled,7);
    PV_PQ(PV_enabled,2)=Sensor_Data_Denise(PV_enabled,8);
    
    ieee37_bus_data1(43,7)=ieee37_bus_data1(43,7)-Sensor_Data_Denise(44,7);
    ieee37_bus_data1(48,7)=ieee37_bus_data1(48,7)-Sensor_Data_Denise(49,7);
    ieee37_bus_data1(53,7)=ieee37_bus_data1(53,7)-Sensor_Data_Denise(54,7);
    ieee37_bus_data1(58,7)=ieee37_bus_data1(58,7)-Sensor_Data_Denise(59,7);
    
    ieee37_bus_data1(43,8)=ieee37_bus_data1(43,8)-Sensor_Data_Denise(44,8);
    ieee37_bus_data1(48,8)=ieee37_bus_data1(48,8)-Sensor_Data_Denise(49,8);
    ieee37_bus_data1(53,8)=ieee37_bus_data1(53,8)-Sensor_Data_Denise(54,8);
    ieee37_bus_data1(58,8)=ieee37_bus_data1(58,8)-Sensor_Data_Denise(59,8);    

    [Volt]=PowerFlowTest12(ieee37_bus_data1, Z);
    Sensor_Data_full=[Sensor_Data PV_PQ Volt(:,2:4)];
    %% Partial sensor data calculation
    PV_PQ(PV_enabled,1)=Generation(t+1);
    PV_PQ(PV_enabled,2)=0;
    Sensor_Data_partial=[Sensor_Data PV_PQ zeros(nNode,3)];    
end



