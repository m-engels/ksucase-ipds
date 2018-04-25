function f = Obj1(x,Num_bus,Pr,Pt,Qr,Qt,iter)
 

if iter~=1
    diff_P=[];diff_Q=[];
    for i=1:Num_bus
%     for i=6:6   
        if ~isempty(Net.SubNode(i).Feeder)
            diff_P(i,:)=((Pr-Pt)/2500).^2;
            diff_Q(i,:)=((Qr-Qt)/2500).^2;
        end
    end
else
    diff_P=[0 0 0;0 0 0];diff_Q=[0 0 0;0 0 0];
end
% 100*sum(diff_P)+100*sum(diff_Q)
% sum(x(3*3*Num_bus+1:3*3*Num_bus+3))
f =sum(x(3*3*Num_bus+1:3*3*Num_bus+3)+100*sum(diff_P)+100*sum(diff_Q)); 
% f =sum(abs(x(3*3*Num_bus+1))+abs(x(3*3*Num_bus+2))+abs(x(3*3*Num_bus+3))); 

end



