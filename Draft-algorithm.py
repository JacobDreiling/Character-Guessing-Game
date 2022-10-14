from math import exp,log2
import csv
from console import clear

# ---- Initialize the game

with open('CharacterTable.csv','r') as f:
	table=list(csv.reader(f)) #needed to change a special character (é)
	questions=list(enumerate(table[0][1:]))
	usedQs=[]
	n=0
	names=[]
	scores=[]
	answers=[]
	for row in table[1:]:
		n+=1
		names+=[row[0]]
		scores+=[0]
		answers+=[row[1:]]

sig=lambda x:1/(1+(n-1)*exp(-x))

def probs(nums):
	total=0
	P=[]
	for n in nums:
		s=sig(n)
		P+=[s]
		total+=s
	return [p/total for p in P]

entropy=lambda P:-sum(p*log2(p) for p in P)
#max entropy = log2(n), when each p = 1/n
#min entropy = 0, when all but one p = 0

''' yes maybe idk no
yes   1   .5   0  -1
maybe .5  .5   0  -.5
idk   0   0    0   0
no   -1  -.5   0   1'''
score_table={
	'Yes':{'Yes':1,'Maybe':.5,'I don\'t know':0,'No':-1},
	'Maybe':{'Yes':.5,'Maybe':.5,'I don\'t know':0,'No':-.5},
	'I don\'t know':{'Yes':0,'Maybe':0,'I don\'t know':0,'No':0},
	'No':{'Yes':-1,'Maybe':-.5,'I don\'t know':0,'No':1}}

def updated(nums,q,ans):
	v=[]
	for i in range(n):
		correct=answers[i][q]
		v+=[nums[i]+score_table[ans][correct]]
	return v

def display():
	clear()
	P=probs(scores)
	m=max(P)
	for i in range(n):
		s=names[i]
		padding=' '*(20-len(s))
		bar='='*int(P[i]/m*80)
		print(s+padding+bar)
	print('Entropy: %f'%entropy(P))

rounds=len(questions) #will probably be set to 20 later

# ---- Play the game

for round in range(rounds):
	display()
	
	# figure out which q to ask
	q_best=s_min=-1
	for i in range(len(questions)):
		# calculate expected entropy
		q,text=questions[i]
		s_avg=0
		for response in ['Yes','No','Maybe','I don\'t know']:
			P=probs(updated(scores,q,response))
			p_ans=0
			for j in range(n):
				match=response==answers[j][q]
				p_ans+=P[j]*match
			s_avg+=entropy(P)*p_ans
		if s_min<0 or s_avg<s_min:
			q_best=i
			s_min=s_avg
	
	# ask q and get response
	q,prompt=questions.pop(q_best)
	usedQs+=[prompt]
	answer=input('Q%d: '%(round+1)+prompt) #this would be a UI input
	
	# update probabilities
	scores=updated(scores,q,answer)

# ---- Finish the game
display()

# pick most likely character
winner=''
best_score=0
for i in range(n):
	if scores[i]>best_score:
		winner=names[i]
		best_score=scores[i]

if input('Is %s your character?'%winner)=='Yes':
	print('Haha I win!')
else:
	print('Dangit you win')
