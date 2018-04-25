function [PA QA PB QB PC QC]=Load1()

IEEE37=[799	7000	1	2	0	721	0	0	0	0	0	0	4
7000	701	2	3	1850	721	101.1111111	55.37037037	101.1111111	55.37037037	101.1111111	55.37037037	4
701	702	3	4	960	722	0	0	0	0	0	0	4
702	703	4	5	1320	722	0	0	0	0	0	0	4
703	727	5	6	240	724	0	0	0	0	20.22222222	10.11111111	4
727	744	6	7	280	723	20.22222222	10.11111111	0	0	0	0	4
744	728	7	8	200	724	20.22222222	10.11111111	20.22222222	10.11111111	20.22222222	10.11111111	4
744	729	7	9	280	724	20.22222222	10.11111111	0	0	0	0	4
703	730	5	10	600	723	0	0	0	0	40.92592593	19.25925926	4
730	709	10	11	200	723	0	0	0	0	0	0	4
709	708	11	12	320	723	0	0	0	0	0	0	4
708	732	12	13	320	724	0	0	0	0	20.22222222	10.11111111	4
708	733	12	14	320	723	40.92592593	19.25925926	0	0	0	0	4
733	734	14	15	560	723	0	0	0	0	20.22222222	10.11111111	4
734	710	15	16	520	724	0	0	0	0	0	0	4
710	735	16	17	200	724	0	0	40.92592593	19.25925926	0	0	4
710	736	16	18	1280	724	0	0	20.22222222	10.11111111	0	0	4
734	737	15	19	640	723	67.40740741	33.7037037	0	0	0	0	4
737	738	19	20	400	723	60.66666667	29.85185185	0	0	0	0	4
738	711	20	21	400	723	0	0	0	0	0	0	4
711	740	21	22	200	724	0	0	0	0	40.92592593	19.25925926	4
711	741	21	23	400	723	0	0	0	0	20.22222222	10.11111111	4
709	731	11	24	600	723	0	0	40.92592593	19.25925926	0	0	4
709	775	11	25	0	721	0	0	0	0	0	0	4
702	705	4	26	400	724	0	0	0	0	0	0	4
705	712	26	27	240	724	0	0	0	0	40.92592593	19.25925926	4
705	742	26	28	320	724	3.851851852	1.925925926	40.92592593	19.25925926	0	0	4
702	713	4	29	360	723	0	0	0	0	40.92592593	19.25925926	4
713	704	29	30	520	723	0	0	0	0	0	0	4
704	714	30	31	80	724	8.185185185	3.851851852	10.11111111	4.814814815	0	0	4
714	718	31	32	520	724	40.92592593	19.25925926	0	0	0	0	4
704	720	30	33	800	723	0	0	0	0	40.92592593	19.25925926	4
720	706	33	34	600	723	0	0	0	0	0	0	4
706	725	34	35	280	724	0	0	20.22222222	10.11111111	0	0	4
720	707	33	36	920	724	0	0	0	0	0	0	4
707	722	36	37	120	724	0	0	67.40740741	33.7037037	10.11111111	4.814814815	4
707	724	36	38	760	724	0	0	20.22222222	10.11111111	0	0	4
718	7181	32	39	125	725	0	0	0	0	0	0	4
7181	7182	39	40	250	725	0	0	0	0	0	0	4
7182	7183	40	41	250	725	0	0	0	0	0	0	4
7183	7184	41	42	250	725	0	0	0	0	0	0	4
7181	7181100	39	43	1	727	0	0	0	0	0	0	4
7181100	71811	43	44	90	726	0	0	0	0	0	0	4
7181100	71812	43	45	90	726	0	0	0	0	0	0	4
7181100	71813	43	46	90	726	0	0	0	0	0	0	4
7181100	71814	43	47	90	726	0	0	0	0	0	0	4
7182	7182100	40	48	1	727	0	0	0	0	0	0	4
7182100	71821	48	49	90	726	0	0	0	0	0	0	4
7182100	71822	48	50	90	726	0	0	0	0	0	0	4
7182100	71823	48	51	90	726	0	0	0	0	0	0	4
7182100	71824	48	52	90	726	0	0	0	0	0	0	4
7183	7183100	41	53	1	727	0	0	0	0	0	0	4
7183100	71831	53	54	90	726	0	0	0	0	0	0	4
7183100	71832	53	55	90	726	0	0	0	0	0	0	4
7183100	71833	53	56	90	726	0	0	0	0	0	0	4
7183100	71834	53	57	90	726	0	0	0	0	0	0	4
7184	7184100	42	58	1	727	0	0	0	0	0	0	4
7184100	71841	58	59	90	726	0	0	0	0	0	0	4
7184100	71842	58	60	90	726	0	0	0	0	0	0	4
7184100	71843	58	61	90	726	0	0	0	0	0	0	4
7184100	71844	58	62	90	726	0	0	0	0	0	0	4
];

PA =IEEE37(:,7);
QA =IEEE37(:,8);
PB =IEEE37(:,9);
QB =IEEE37(:,10);
PC =IEEE37(:,11);
QC =IEEE37(:,12);
