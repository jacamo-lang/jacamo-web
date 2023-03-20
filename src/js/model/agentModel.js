/**
  * MODELS
  */

export class Agent {
    constructor(name) {
        this.name = name;
    }

    setRoles(roles) {
        this.roles = roles;
    }

    setMissions(missions) {
        this.missions = missions;
    }

    setBeliefs(beliefs) {
        this.beliefs = beliefs;
    }

    setAslFiles(aslFiles) {
        this.aslFiles = aslFiles;
    }
}

export class Belief {
    constructor(predicate) {
        this.predicate = predicate;
    }
}