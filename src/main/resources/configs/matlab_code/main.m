% updated main.m from Ahmad 9 Feb 2014

clc
close all
clear all

%%
% disp('Insert the time slice');
% t=input(' t= ');
% disp('Insert the matrix of sensor Data (a matrix with 62 rows and 11 columns ');
% disp('if time slice is 1 insert [] ');
% 
% Sensor_Data_Denise=input('Sensor_Data_Denise= ');
%%
PV_candidate=[44 49 54 59];
T=5;
for t=1:T
    if t==1
        Sensor_Data_Denise=[];
    end
    [Sensor_Data_full,Sensor_Data_partial]=get_Voltage(Sensor_Data_Denise,t);
    Sensor_Data_Denise=Sensor_Data_partial;
    Sensor_Data_Denise(PV_candidate,8)=(Sensor_Data_partial(PV_candidate,1)-Sensor_Data_full(PV_candidate,1))+...
        (Sensor_Data_partial(PV_candidate,2)-Sensor_Data_full(PV_candidate,2))-...
        (Sensor_Data_partial(PV_candidate,7)-Sensor_Data_full(PV_candidate,7))+...
        Sensor_Data_full(PV_candidate,8);
end