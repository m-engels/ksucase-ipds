function [Net]=ReadSensorData(xlspath_sys, xlspath_sun)


ieee37_temp=PowerSystemDataOPF(xlspath_sys, xlspath_sun);

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


end