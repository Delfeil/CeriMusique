deploy_analyseur:
	cd ./analyseur_entites_nommees && ./deploy.sh

clear_analyseur:
	cd ./analyseur_entites_nommees && ./clear.sh

deploy_player:
	cd ./player && ./deploy.sh

clear_player:
	cd ./player && ./clear.sh

start_player:
	cd ./player && ./start.sh

deploy_speech_to_text:
	cd ./speech_to_text && ./deploy.sh

clear_speech_to_text:
	cd ./speech_to_text && ./clear.sh

start_speech_to_text:
	cd ./speech_to_text && ./start.sh

mrproper: clear_player clear_analyseur clear_speech_to_text