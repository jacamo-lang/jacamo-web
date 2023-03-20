/**
  * MODELS
  */
 
export class Workspace {
    constructor(name, fullName, artifacts) {
        this.name = name;
        this.fullName = fullName;
        this.artifacts = artifacts;
    }

    setArtifacts(artifacts) {
        this.artifacts = artifacts;
    }
}

export class Artifact {
    constructor(name, type) {
        this.name = name;
        this.type = type;
    }
}
