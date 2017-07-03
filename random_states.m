function[sample_states] = random_states(num_rows, num_cols, k_row, k_col,ranks_rows,ranks_cols)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%num_rows and num_cols = size of full matrix (900x(600*21) in this case)
%k_row and k_col = the number of samples to be picked per row and column
%ranks_rows = how many times we are partitioning the full matrix along the 
%                    columns (i.e number of submatrices from left to right)
%ranks_cols = the number of submatrices from top to bottom
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


diary('diary.txt');
% parallel = 1;
% if(parallel == 1)  % checking to see if my pool is already open
%     parpool(16);
% end
addpath('/opt/ilog/cplex/matlab');
rng('shuffle');
global A_part_glob sample_states_part_glob I_glob LeastSq_glob;
size_row = num_rows/ranks_rows; %number of rows for each submatrix
size_col = num_cols/ranks_cols; %number of columns for each submatrix
num_sub = ranks_rows*ranks_cols; %number of submatrices
A_part = zeros(size_row,size_col);  %binary matrix where entry is 1 if we sample, 0 otherwise (size of submatrix and is the same for each submatrix)
sample_states_part_row = zeros(size_row*k_row,2); %indices of row-wise sampled states for submatrices
sample_states_part_col = zeros(size_col*k_col,2); %indices of col-wise sampled states for submatrices
%fill up sampled states of the submatrices and binary matrix A_part

%pick k_row samples per row
for row_idx = 1:size_row
    X = randperm(size_col, k_row);
    A_part(row_idx, X) = 1;
    sample_states_part_row((row_idx-1)*k_row+1:row_idx*k_row,:)  = [row_idx.*ones(k_row,1),X'];
end
%pick k_col samples per col
for col_idx = 1:size_col
    X = randperm(size_row, k_col);
    A_part(X, col_idx) = 1;
    sample_states_part_col((col_idx-1)*k_col+1:col_idx*k_col,:)  = [X',col_idx.*ones(k_col,1)];
end
% concatenate the two sample vectors and remove the duplicated entries
sample_states_part = vertcat(sample_states_part_row, sample_states_part_col);
sample_states_part = unique(sample_states_part,'rows');


size_jump = size(sample_states_part,1); % sample size for each individual submatrix
sample_size = size_jump*num_sub; % total sample size for all the submatricies
sample_states = zeros(sample_size,2); %indices of sampled states of full matrix
sub = 1;
%for each submatrix, find the indices of the sampled states according to
%A_part, which we are just shifting.
for rank2 = 1:ranks_cols
    start_col = 1+size_col*(rank2-1);
    col_idx_offset = sample_states_part(:,2) + (start_col-1)*ones(size_jump,1);
    for rank1 = 1:ranks_rows
        start_row = 1+size_row*(rank1-1);
        sample_states(sub:sub+size_jump-1,1) = sample_states_part(:,1) + (start_row-1)*ones(size_jump,1); 
        sample_states(sub:sub+size_jump-1,2) = col_idx_offset; 
        sub = sub+size_jump;
    end
end
% -1 since Java is 0-indexing
sample_states = sample_states - ones(size(sample_states));

% we have |size_jump| number of samples per submatrix.
% we want: min_x || I x - V(i,j) ||, where 
% the length of decision x = size_row+size_col. 
% if we have 4 decisions variable (2 row, 2 col), and 2 samples at
% (1,1),(1,2), then I looks like
% [1, 0, 1, 0; 
%  1, 0, 0, 1];
I_row = zeros(size_jump,size_row);
I_col = zeros(size_jump,size_col);
ind_r = sub2ind(size(I_row),(1:size_jump)',sample_states_part(:,1));
ind_c = sub2ind(size(I_col),(1:size_jump)',sample_states_part(:,2));
I_row(ind_r) = 1;
I_col(ind_c) = 1;
I = horzcat(I_row,I_col);

% pseudoinverse used for the L-2 penalty
LeastSq = pinv(I'*I)*I';

% forming the constraint matrix for the LP (L-1 penalty)
% we need to solve:
% min sum_i epsilon_i
% s.t.  I x - V <= epsilon
%      -I x + V >= -epsilon
I = vertcat(I, -I);
I = horzcat(I, -vertcat(eye(size_jump), eye(size_jump)));

A_part_glob = A_part;
sample_states_part_glob = sample_states_part;
I_glob = I;
LeastSq_glob = LeastSq;
diary off;
end
