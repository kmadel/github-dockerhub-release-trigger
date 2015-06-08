stage 'Parse GitHub Payload'
payloadObject = new groovy.json.JsonSlurper().parseText(${payload})
 
def tagName = payloadObject.release.tag_name
def repoName = payloadObject.repository.full_name
def gitUrl = payloadObject.repository.git_url

echo "release tag: " + tagName
echo "repoName name: " + repoName
echo "gitUrl: " + gitUrl
node('docker') {
	docker.withServer('tcp://127.0.0.1:1234'){
	  docker.withRegistry('https://registry.hub.docker.com/', 'docker-registry-kmadel-login') {
	  	git url: '${gitUrl}'
	  	checkout scm: [$class: 'GitSCM', 
	  	  userRemoteConfigs: [[url: '${gitUrl}']], 
        branches: [[name: 'refs/tags/${tagName}']]], changelog: false, poll: false
	  	def image = docker.build "${repoName}:${tagName}"
	  	stage 'Push Image ${repoName}:${tagName}'
	  	image.push '${tagName}'
	}
}
