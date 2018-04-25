function [vv]=PowerFlowTest12(ieee37_bus_data, Z)


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
nnode=max(ieee37_bus_data(:,4)); 
nbranch=nnode-1;
ftmi =0.000189394;
sb = 2500000;
vb = 4800;
zb = (vb^2)/sb;
Ib=vb/zb;
PhaseToLine=[1 0 -1;-1 1 0;0 -1 1];
%%%%%% Z line
znode=zeros(3,nnode);
zline =cell(nnode,nnode);

for i = 1:nbranch
    %i
    if ieee37_bus_data(i,6)>700
        zline{ieee37_bus_data(i,3),ieee37_bus_data(i,4)} = ieee37_bus_data(i,5)*ftmi*cell2mat(Z(ieee37_bus_data(i,6)-720));%./zb;
    end
end
zline{11,25}=zeros(3,3);



%%%%%% apparent power
S=zeros(3,nnode);
for i=1:nnode-1
    S(:,ieee37_bus_data(i,4))=1000*[(ieee37_bus_data(i,7))+1j*(ieee37_bus_data(i,8))
                                    (ieee37_bus_data(i,9))+1j*(ieee37_bus_data(i,10))
                                    (ieee37_bus_data(i,11))+1j*(ieee37_bus_data(i,12))];
end
% S(1:3,:)=S(1:3,:)./sb;


%% Connection Matrix

ConnectMatrix=zeros(nnode,nnode);
ConnectMatrix(1,1)=1;ConnectMatrix(nbranch,nbranch)=1;
for i=1:nbranch
    ConnectMatrix(ieee37_bus_data(i,4),ieee37_bus_data(i,4))=1;
    if ieee37_bus_data(i,6)>1
        ConnectMatrix(ieee37_bus_data(i,3),ieee37_bus_data(i,4))=1;
%     elseif ieee37_bus_data(i,6)==1
%         ConnectMatrix(ieee37_bus_data(i,3),ieee37_bus_data(i,4))=0.5;
%         ConnectMatrix(ieee37_bus_data(i,3),ieee37_bus_data(i,4))=1;
    end
end


% Voltage Matrix
Vbus=zeros(3,nnode);VLLbus=zeros(3,nnode);
Iline=cell(nnode,nnode);
Vnom=4800*[exp(-1j*30*pi/180); exp(-1j*150*pi/180); exp(1j*90*pi/180)]/sqrt(3);  
% VLLnom=PhaseToLine'*Vnom;
% VLLnom=4800*[exp(1j*30*pi/180); exp(-1j*90*pi/180); exp(1j*150*pi/180)]; 
VLLnom=4800*[1; exp(-1j*120*pi/180); exp(1j*120*pi/180)]; 
% PhaseToLine'*Vnom-4800*[exp(1j*30*pi/180); exp(-1j*90*pi/180); exp(1j*150*pi/180)]
for i = 1:nnode 
    if sum(ConnectMatrix(i,:)) == 1 || sum(ConnectMatrix(i,:))==0.5% Terminal Node
       Vbus(:,i)=Vnom; 
    end
    VLLbus(:,i)=VLLnom;
end
% VLLbus(:,38)=[0.975*(cos(-0.33*(pi/180))+1j*sin(-0.33*(pi/180)))
%             0.97*(cos(-120.65*(pi/180))+1j*sin(-120.65*(pi/180)))
%             0.968*(cos(119.76*(pi/180))+1j*sin(119.76*(pi/180)))]*4800;
% %         
% VLLbus(:,35)=[0.977*(cos(-0.24*(pi/180))+1j*sin(-0.24*(pi/180)))
%             0.976*(cos(-120.68*(pi/180))+1j*sin(-120.68*(pi/180)))
%             0.97*(cos(119.61*(pi/180))+1j*sin(119.61*(pi/180)))]*4800;
        %Vbus(:,38)
% VLLbus(:,38)-PhaseToLine'*XXX

% VLLbus(:,38)=PhaseToLine'*Vbus(:,38);
iter=0;

tol=10;
VVLLbus=zeros(size(Vbus))';
IIflow=[];
while tol>1e-4
    iter=iter+1;%iter;
%                                              Backward Sweep
    for i=nnode:-1:2
        %i
        temp1=find(ConnectMatrix(i,:)==1);
        temp2=find(ConnectMatrix(:,i)==1);
        %1 is D-PQ, 2 is D-Z, 3 is D-I
        if ieee37_bus_data(find(ieee37_bus_data(:,4)==temp2(2)),13)==1 ||ieee37_bus_data(find(ieee37_bus_data(:,4)==temp2(2)),13)==0
            Iline{temp2(1),temp2(2)}=PhaseToLine*conj(S(:,temp2(2))./VLLbus(:,temp2(2)));
        elseif ieee37_bus_data(find(ieee37_bus_data(:,4)==temp2(2)),13)==2
            Iline{temp2(1),temp2(2)}=PhaseToLine*(VLLbus(:,temp2(2))./((abs(VLLnom).^2)./conj(S(:,temp2(2)))));
        elseif ieee37_bus_data(find(ieee37_bus_data(:,4)==temp2(2)),13)==3
            Iline{temp2(1),temp2(2)}=PhaseToLine*(abs(conj(S(:,temp2(2))./VLLnom)).*exp((1j*angle(conj(S(:,temp2(2))./VLLbus(:,temp2(2)))))));
        elseif ieee37_bus_data(find(ieee37_bus_data(:,4)==temp2(2)),13)==4
            Iline{temp2(1),temp2(2)}=conj(S(:,temp2(2))./VLLbus(:,temp2(2)));
        end
%         Iline{temp2(1),temp2(2)}=PhaseToLine*conj(S(:,temp2(2))./VLLbus(:,temp2(2)));
%         [abs(Iline{temp2(1),temp2(2)})' (angle(Iline{temp2(1),temp2(2)})*(180/pi))']
%         temp2(1),temp2(2)
        for k=1:length(temp1)-1
            Iline{temp2(1),temp2(2)}=cell2mat(Iline(temp2(1),temp2(2)))+cell2mat(Iline(temp2(2),temp1(k+1)));
%          temp2(2),temp1(j+1)
        end
        %Vbus(:,temp2(1))=Vbus(:,temp2(2))+(cell2mat(zline(temp2(1),temp2(2)))*cell2mat(Iline(temp2(1),temp2(2))));
    end
    %     tol=abs(Vbus(:,1)-vnom);
    
    %                                                      Forward Sweep
      Vbus(:,1)=Vnom;
%     Vbus(:,1)=Vnom.*[1.0437  1.0250  1.0345 ]';
%     VLLbus(:,1)=VLLnom.*[1.0437  1.0250  1.0345 ]';
%     Vbus(:,1)=PhaseToLine*VLLbus(:,1)/3;
%     Vbus(:,1)=Vnom.*[1.0391  1.0344  1.0298 ]';
    VLLbus(:,1)=PhaseToLine'*Vbus(:,1);
   % IIflow=[];
    for i=1:nnode
        %i
        temp1=find(ConnectMatrix(i,:)==1);
        for k=1:length(temp1)-1
            Vbus(:,temp1(k+1))=Vbus(:,temp1(1))-(cell2mat(zline(temp1(1),temp1(k+1)))*cell2mat(Iline(temp1(1),temp1(k+1))));
            IIflow=[IIflow; temp1(1),temp1(k+1) abs(cell2mat(Iline(temp1(1),temp1(k+1))))' (angle(cell2mat(Iline(temp1(1),temp1(k+1))))*(180/pi))'];
%             temp1(1),temp1(j+1)
%             abs(Vbus(:,temp1(1)))'./([4800 4800 4800]/sqrt(3))
%             abs(Vbus(:,temp1(j+1)))'./([4800 4800 4800]/sqrt(3))
            %temp1(1),temp1(j+1)
            tol;
        end
        VLLbus(:,i)=PhaseToLine'*Vbus(:,i);
    end
    VVLLbus=[VVLLbus;VLLbus'];

    
%     [abs(Vbus(:,35))'./([4800 4800 4800]/sqrt(3)) (angle(Vbus(:,35))*(180/pi))']
%     [abs(VLLbus(:,35))'./([4800 4800 4800]) (angle(VLLbus(:,35))*(180/pi))']
    tol=max(max(abs(VVLLbus(1+(iter-1)*nnode:iter*nnode,:)-VVLLbus(1+iter*nnode:(iter+1)*nnode,:))));
    
    
end

Iflow=[abs(cell2mat(Iline))' (angle(cell2mat(Iline))*(180/pi))'];

v = [(1:nnode)' sqrt(3)*(abs(Vbus)'./repmat([4800 4800 4800],nnode,1)) (angle(Vbus)*(180/pi))'];
vv = [(1:nnode)' (abs(VLLbus)'./repmat([4800 4800 4800],nnode,1)) (angle(VLLbus)*(180/pi))'];

%v = [(1:nnode)' (abs(VVbus)) (angle(VVbus)*(180/pi))];

v = {v};
