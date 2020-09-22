# BePT

BePT is a behavior-based process model interpreter. It takes a process model (such as Petri Net) as input, and outputs the behavioral descriptions of the model.

You can refer to the CIKM 2019 paper “***BePT: A Behavior-based Process Translator for Interpreting and Understanding Process Models***” for more details.

This is the project of the simplified BePT, concering mainly on model linearization. Interested readers can refer to [Goun](https://github.com/qianc62/Goun "Goun") for more details about how to process Deep-Syntactic-Tree (DSynT).

## Overview

- code/ 
  contains the source codes.
- data/ 
  contains some process model examples used for evaluating.

### Reqirements:

* Python-3.0 (Simplified)
or
* Java-1.8.0 
* Stanford_POS_Tagger-2011-06-19
* WordNet-3.0

### Citation

If you find this project helps, please kindly consider citing this paper as:

```
@inproceedings{Goun,
	title = {BePT: A Behavior-based Process Translator for Interpreting and Understanding Process Models},
	author = {Chen Qian and Lijie Wen and Ahkil Kumar},
	booktitle = {Proceedings of The 28th ACM International Conference on Information and Knowledge Management (CIKM)},
	year = {2019},
	pages = {1873–1882}
}
```
