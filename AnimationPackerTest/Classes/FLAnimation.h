#pragma once

#include <vector>
#include <string>
#include <map>
#include "cocos2d.h"
#include "sprite_nodes\CCSprite.h"

using namespace std;
using namespace cocos2d;

struct FLModule
{
	int x;
	int y;
	int trans;
	string plist;
	string clip;
	CCSprite *displayable;
};

struct FLFrame
{
	string name;
	vector<FLModule*> modules;
};

struct FLSequence
{
	int duration;
	int frameId;
};

struct FLAction
{
	string name;
	vector<FLSequence*> sequences;
};

class FLAnimation : public CCSprite
{
public:
	string name;
	vector<FLAction*> actions;
	vector<FLFrame*> frames;
	void initAnimation();
	void setAction(int id);
	void nextAction();
	void nextFrame();
	void setSequence(int id);
	void update(float dt);
private:
	int currentAction;
	int currentSequence;
	int currentDuration;
};

class AnimationSaxDelegator : public CCSAXDelegator
{
public:
	enum{
		STATE_NONE,
		STATE_PLIST,
		STATE_ANIMATION,
		STATE_FRAMES,
		STATE_FRAME,
		STATE_MODULE,
		STATE_ACTIONS,
		STATE_ACTION,
		STATE_SEQUENCE,
		STATE_NAME,
		STATE_DELAY,
		STATE_FLIP_X,
		STATE_FLIP_Y,
		STATE_SPRITE_FRAME
	}state;
	void startElement(void *ctx, const char *name, const char **atts) ;
	void endElement(void *ctx, const char *name) ;
	void textHandler(void *ctx, const char *s, int len) ;

	FLAnimation *_anim;
	FLFrame *_frame;
	FLAction *_action;
	FLSequence *_sequence;
	FLModule *_module;
	vector<string> plists;
	vector<FLAnimation*> animates;
};

class AnimationPacker
{
public:
	static AnimationPacker* getInstance();
	FLAnimation* loadAnimations(const char *path);
	void freeAnimations(const char *path);
private:
	map<string, FLAnimation> nameToAnimationMap;
	map<string, vector<string>> pathToPlistsMap;
	map<string, set<string>> pathToNameMap;
};
