function ttda(a, b, c, d)
% Two-Tier Double Auction MICAI

rand('seed',1);

%%%%%%%%%%%%%%%%%%%%%%%%%% Each Neighborhood k

if nargin<4 
    disp('Please input all 4 homes IDs')
end
N=4; % Number of Neighborhoods
NB=3; % Number of buyers at each Neighborhood, 3 for all neighborhoods for this test case.
NS=1; % Number of sellers at each Neighborhood, 1 for all neighborhoods for this test case.

for k=1:N
    demand(k,:)= 2+6*rand(1,NB); % demand matrix of each neighborhood. Rows show neighborhood k, column shows homes in neighborhood k.
    supply(k,:)= (NB/NS)*(2+6*rand(1,NS));% supply matrix of each neighborhood. Rows show neighborhood k, column shows homes in neighborhood k.
    bid(k,:)= (demand(k,:))./(10*min(demand(k,:))); % The factor in the denom. is to convert demands to cent values of around 10 cents.
    ask(k,:)= 0.11*max((NB/NS)*(supply(k,:)))./((NB/NS)*(supply(k,:))); % The numerator is to convert supplies to cent values of around 11. ( 1.1*10)
    % Neighborhood clearing price vector 1xN
    c0k(k,:)= (ask(k,:)+mean(bid(k,:)))/2; % Since the number of home agents in each neighborhood is limited to 4, just an adhoc calculation of the
    %%%market clearing price is sufficient.
    f(k,:)=-1*[bid(k,:)-c0k(k,:) c0k(k,:)-ask(k,:)];
    lb=zeros(1,NB+NS);
    ub=[demand(k,:) supply(k,:)];
    [x(k,:)]=linprog(f(k,:),[],[],[],[],lb,ub);
    if k==N
        x1=x';
        x1(1:NB,:)=-1*x1(1:NB,:);
        surdef=sum(x1);
        surdef=surdef';
    end   
end
demand1=demand';
Demand=sum(demand1); % Vector of Total demand of all neighborhoods.
Demand=Demand';
Supply=supply; % Vector of Total supply of all neighborhoods. Changes shall be made, like demand, when more than one seller exists in a neighborhood k.

%%%%%%%%%%%%%%%%%%%%%%%%%%% Each Grid level Feeder
bk= surdef./100;          % External bid of neighborhood k to other neighborhoods based on their suplus/deficit power.
c0=mean(c0k);
vecc0=repmat(c0,4,1);
ff=-[c0k-c0]';
lbf=zeros(N,1);
ubf=abs(surdef);
Aeq=[]; 
beq=[]; 
[y]=linprog(ff,[],[],Aeq,beq,lbf,ubf);
y(surdef<0)=-1*y(surdef<0);

c0kv1=repmat(c0k(1,:),4,1);
c0kv2=repmat(c0k(2,:),4,1);
c0kv3=repmat(c0k(3,:),4,1);
c0kv4=repmat(c0k(4,:),4,1);
if a==43 && b==48 && c==53 && d==58
    disp('L39 Power Allocation to Neighborhoods N43,N48,N53,N58')
    disp('demands/supplies of Neighborhoods, with negatives showing demand:')
    disp(surdef)
    disp('bids/asks')
    disp(c0k)
    disp('Power Allocation   Market Price')
    disp([y vecc0]);
elseif a==44 && b==45 && c==46 && d==47 
    disp('N43 Power Allocation to homes H44,H45,H46,H47')
    disp('demands of buyer Homes at Neighborhood:')
    disp(demand(1,:))
    disp('Supply of seller Home at the neighborhood:')
    disp(supply(1))
    disp('bids of buyer Homes at the Neighborhood')
    disp(bid(1,:))
    disp('ask of seller Home at the Neighborhood')
    disp(ask(1))
    disp('Power Allocation   Market Price')
    disp([x(1,:)' c0kv1]);
elseif a==49 && b==50 && c==51 && d==52
    disp('N48 Power Allocation to homes H49,H50,H51,H52')
    disp('demands of buyer Homes at Neighborhood:')
    disp(demand(2,:))
    disp('Supply of seller Home at the neighborhood:')
    disp(supply(2))
    disp('bids of buyer Homes at the Neighborhood')
    disp(bid(2,:))
    disp('ask of seller Home at the Neighborhood')
    disp(ask(2))
    disp('Power Allocation   Market Price')
    disp([x(2,:)' c0kv2]);
elseif a==54 && b==55 && c==56 && d==57
    disp('N53 Power Allocation to homes H54,H55,H56,H57')
    disp('demands of buyer Homes at Neighborhood:')
    disp(demand(3,:))
    disp('Supply of seller Home at the neighborhood:')
    disp(supply(3))
    disp('bids of buyer Homes at the Neighborhood')
    disp(bid(3,:))
    disp('ask of seller Home at the Neighborhood')
    disp(ask(3))
    disp('Power Allocation   Market Price')
    disp([x(3,:)' c0kv3])
elseif a==59 && b==60 && c==61 && d==62
    disp('N48 Power Allocation to homes H59,H60,H61,H62')
    disp('demands of buyer Homes at Neighborhood:')
    disp(demand(4,:))
    disp('Supply of seller Home at the neighborhood:')
    disp(supply(4))
    disp('bids of buyer Homes at the Neighborhood')
    disp(bid(4,:))
    disp('ask of seller Home at the Neighborhood')
    disp(ask(4))
    disp('Power Allocation   Market Price')
    disp([x(4,:)' c0kv4])
else
    disp('Input correct 4 homes/Neighborhoods ID')
end


end