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