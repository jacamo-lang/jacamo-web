/**
  * MODELS
  */
 export class Organisation {
    constructor(name, groups, schemes) {
        this.name = name;
        this.groups = groups;
        this.schemes = schemes;
    }

    setGroups(groups) {
        this.groups = groups;
    }

    setSchemes(schemes) {
        this.schemes = schemes;
    }
}

export class Group {
    constructor(id) {
        this.id = id;
    }
}

export class Scheme {
    constructor(scheme) {
        this.scheme = scheme;
    }
}

export class Agent {
    constructor(name, roles, missions) {
        this.name = name;
        this.roles = roles;
        this.missions = missions;
    }

    setRoles(roles) {
        this.roles = roles;
    }

    setMissions(missions) {
        this.missions = missions;
    }
}

export class Role {
    constructor(role,group){
        this.role = role;
        this.group = group;
    }
}

export class Mission {
    constructor(mission, scheme, responsibles) {
        this.mission = mission;
        this.scheme = scheme;
        this.responsibles = responsibles;
    }
}