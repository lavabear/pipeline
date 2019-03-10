import React from 'react';

import {Command, CommandArgument } from "./Command";

function NewCommand(selected) {
    return <div className="NewCommand">
        <h3>{selected.command}</h3>
        <div className="Arguments">
            { selected.arguments.map(args =>
                <CommandArgument key={args.name} name={args.name} type={args.type} required={args.required} />) }
        </div>
    </div>
}

export function Home(commands, pipelineRequest, selected, changeSelected) {
    var i = 0;
    return <div className="Home">
        <label>Start:<textarea /></label>
        <select onChange={changeSelected}>
            { commands.map(command => <option key={i} value={i++}>{command.command}</option>) }
        </select>
        { pipelineRequest.map(Command) }
        { selected !== undefined ? NewCommand(selected) : '' }
        <button>Add</button>
        <button>Save</button>
    </div>;
}