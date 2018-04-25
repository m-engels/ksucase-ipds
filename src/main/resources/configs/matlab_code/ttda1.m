function [result]=ttda1(A)
% Two-Tier Double Auction MICAI
%%%%%%%%%%%%%%%%%%%%%%%%%% Each Neighborhood k
% [A,I]=sortrows(A,5);
N=4; % Number of Neighborhoods
% NB=length(A(find(A(:,4)==1),4)); % Number of buyers at each Neighborhood, 3 for all neighborhoods for this test case.
% NS=length(A(find(A(:,4)==0),4)); % Number of sellers at each Neighborhood, 1 for all neighborhoods for this test case.
NB=3;
NS=1;
B=zeros(NB+NS,5);
a=find(A(:,4)==1);
b=find(A(:,4)==0);
B(NB+1:NB+NS,:)=A(a,:);
B(1:NB,:)=A(b,:);
demand = B(1:NB,5); % demand matrix of each neighborhood. Rows show neighborhood k, column shows homes in neighborhood k.
supply = B(NB+1:NB+NS,5);
buy= B(1:NB,2); % The factor in the denom. is to convert demands to cent values of around 10 cents.
sell= 1.01*mean(buy);
% Neighborhood clearing price vector 1xN
c0k= (mean(sell)+mean(buy))/2; % Since the number of home agents in each neighborhood is limited to 4, just an adhoc calculation of the
%%%market clearing price is sufficient.
f=-1*[(buy-c0k)' sell-c0k];
lb=zeros(NB+NS,1);
ub=[demand; supply];
x=linprog(f,[],[],[],[],lb,ub);
c0kvector=repmat(c0k,4,1);
result=zeros(NB+NS,1);
a1=find(B(:,4)==1);
b1=find(B(:,4)==0);
result(a)=x(a1);
result(b)=x(b1);
r=result;
result=[result c0kvector];

a=r<max(r);
%
def=a.*r;
def=sum(def);
surdef=max(r)-def;
end