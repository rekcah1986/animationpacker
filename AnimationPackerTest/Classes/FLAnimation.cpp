#include "platform\CCSAXParser.h"
#include "FLAnimation.h"

using namespace std;
using namespace cocos2d;

void FLAnimation::initAnimation()
{
	CCSprite::init();
	for(UINT j=0;j<frames.size();j++)
	{
		FLFrame* frame = frames[j];
		for(UINT i=0;i<frame->modules.size();i++)
		{
			FLModule *module = frame->modules[i];
			const char* spriteName = module->clip.c_str();
			CCSpriteFrame *f = CCSpriteFrameCache::sharedSpriteFrameCache()->spriteFrameByName(spriteName);
			CCSprite* s = CCSprite::create();
			s->setTexture(f->getTexture());
			s->setTextureRect(f->getRect());
			s->setPositionX(module->x);
			s->setPositionY(module->y);
			s->retain();
			module->displayable = s;
			module->displayable->setParent(NULL);
		}
	}
	setAction(0);
	this->scheduleUpdateWithPriority(100);
}

void FLAnimation::setAction(int id)
{
	FLAction *action = actions.at(id);
	currentAction = id;
	currentSequence = 0;
	currentDuration = 0;
	setSequence(0);
}

void FLAnimation::nextAction()
{
	int actionId = currentAction + 1;
	if(actionId >= this->actions.size())
	{
		actionId = 0;
	}
	setAction(actionId);
}

void FLAnimation::nextFrame()
{
	FLAction* action = actions[currentAction];
	if(currentSequence < 0 || currentSequence >= action->sequences.size())
	{
		return;
	}
	FLSequence* sequence = action->sequences[currentSequence];
	if(sequence && currentDuration++ < sequence->duration)
	{
		return;
	}
	currentDuration = 0;
	if(++currentSequence >= action->sequences.size())
	{
		currentSequence = 0;
	}
	setSequence(currentSequence);
}

void FLAnimation::setSequence(int id)
{
	FLAction* action = actions[currentAction];
	if(currentSequence < 0 || currentSequence >= action->sequences.size())
	{
		return;
	}
	FLSequence* sequence = action->sequences[currentSequence];
	if(sequence->frameId < 0)
	{
		return;
	}
	this->removeAllChildrenWithCleanup(false);
	FLFrame *frame = frames[sequence->frameId];
	for(int i=0;i<frame->modules.size();i++)
	{
		FLModule* module = frame->modules[i];
		this->addChild(module->displayable);
	}
}

void FLAnimation::update(float dt)
{
	this->nextFrame();
}

CCString getAtts(const char** atts, const char* name)
{
	for(int i=0;atts[i];i+=2)
	{
		if(strcmp(atts[i], name) == 0)
		{
			return CCString(atts[i + 1]);
		}
	}
	return CCString();
}

void AnimationSaxDelegator::startElement( void *ctx, const char *name, const char **atts )
{
	string tag((char*)name);
	if(tag == "Plist")
	{
		state = STATE_PLIST;
	}
	else if(tag == "Animation")
	{
		_anim = new FLAnimation();
		state = STATE_ANIMATION;
		animates.push_back(_anim);
	}
	else if(tag == "Frame")
	{
		string str;
		state = STATE_FRAME;
		_frame = new FLFrame();
		_frame->name = getAtts(atts, "name").getCString();
		_anim->frames.push_back(_frame);
		CCLog("frame %s %d", _frame->name.c_str(), _frame);
	}
	else if(tag == "Module")
	{
		state = STATE_MODULE;
		_module = new FLModule();
		_module->plist = getAtts(atts, "plist").getCString();
		_module->clip = getAtts(atts, "clip").getCString();
		_module->x = getAtts(atts, "x").intValue();
		_module->y = getAtts(atts, "y").intValue();
		_module->trans = getAtts(atts, "trans").intValue();
		_frame->modules.push_back(_module);
	}
	else if(tag == "Sequence")
	{
		state = STATE_SEQUENCE;
		_sequence = new FLSequence();
		_sequence->duration = getAtts(atts, "duration").intValue();
		_sequence->frameId = getAtts(atts, "id").intValue();
		_action->sequences.push_back(_sequence);
	}
	else if(tag == "Action")
	{
		state = STATE_ACTION;
		_action = new FLAction();
		_anim->actions.push_back(_action);
	} else {
		CCLog("startElement %s", name);
	}
}

void AnimationSaxDelegator::endElement( void *ctx, const char *name )
{
	state = STATE_NONE;
}

void AnimationSaxDelegator::textHandler( void *ctx, const char *ch, int len )
{
	if(state == STATE_NONE)
	{
		return;
	}
	string text((char*)ch, 0, len);
	switch(state)
	{
	case STATE_PLIST:
		plists.push_back(text);
		break;
	}
}

AnimationPacker *_instance;
AnimationPacker *AnimationPacker::getInstance() {
	if(NULL == _instance)
	{
		_instance = new AnimationPacker();
	}
	return _instance;
}

FLAnimation* AnimationPacker::loadAnimations(const char *path)
{
	const char *pszPath = CCFileUtils::sharedFileUtils()->fullPathFromRelativePath(path);
	CCSAXParser parser;
	AnimationSaxDelegator delegator;
	if(!parser.init("UTF-8"))
	{
		return NULL;
	}
	parser.setDelegator(&delegator);
	parser.parse(pszPath);
	delegator._anim->name = path;
	// load plist
	vector<string> plists = delegator.plists;
	for(UINT i=0;i<plists.size();i++)
	{
		string plistPath = CCFileUtils::sharedFileUtils()->fullPathFromRelativeFile(plists[i].c_str(), pszPath);
		CCSpriteFrameCache::sharedSpriteFrameCache()->addSpriteFramesWithFile(plistPath.c_str());
	}
	delegator._anim->initAnimation();
	return delegator._anim;
}

void AnimationPacker::freeAnimations(const char *path)
{
}
