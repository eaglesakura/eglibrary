# eglibrary

## ���̃��C�u�����ɂ���

Android����OpenGL ES 1.1���b�p�[���C�u�����ł��B
2D�Q�[���̐���ɕK�v�ȕ`�����̏����A���w�����A�X���b�h����n�̕⏕�N���X����ʂɂ���܂��B
��{�I��@eaglesakura���l�I�Ɏg��or�g�������@�\�݂̂ō\������Ă��܂��B
�\�[�X�R�[�h�̓��C�Z���X�ɏ]���Ď��R�Ɏg���Ă�����č\���܂��񂪁A�ǂ�ȕs��������Ă��ӔC�͎����܂���B
OpenGL�֘A�̃T���v���\�[�X�Ƃ��Č��邭�炢�����x�����̂ł͂Ȃ����Ǝv���܂��B

## �g����

�K�v�ȃv���W�F�N�g��Android�̃��C�u�����v���W�F�N�g�Ƃ��Ēǉ����邩�A�r���h�p�X��release/�z����jar��ǉ����Ă��������B
jar�ɂ̓\�[�X�R�[�h���܂܂�Ă��܂��̂ŁA�����ŃG���[���������Ă��g���[�X���ł��邩�Ǝv���܂��B

## ���m�̖��jMali�nGPU�ł̕s�

���̏������ŁAMali�n��GPU�iGalaxy S2���j�Ɍ���EGL�̕��A�Ɏ��s����悤�ł��B
���̂Ƃ���Qualcomm�n��GPU�iNexus One���j�ł͔������܂���B

1. OpenGLManager�N���X��OpenGLView���g���Ă���Activity����ʂ�Activity���Ăяo��
1. ��ʑJ�ڒ�����u�߂�v�L�[��A�ł��A������Activity�֖߂�
1. OpenGLView��EGL_BAD_ALLOC���������AOpenGL ES�̕`�悪�s���Ȃ��i���f����Ȃ��j
1. ��L�̏�ԂɂȂ����ꍇ�A���݂̂Ƃ����O�𓊂���悤�ɂ��Ă��邽�߁A���o�͉\
1. ���̂悤�ȏ�ԂɂȂ�����A�ēxActivity��Surface�̔j���E�����iAcrtivity��onPause/onResume���j���s�����Ƃŕ����ł���