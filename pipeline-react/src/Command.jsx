import React from 'react';

export function CommandArgument(props) {
    return <div className="CommandArgument">
        <h3>{props.name}</h3>
        <h4>{props.type}</h4>
        <h3>{props.required}</h3>
    </div>;
}

function DataType(props) {
    return <div className="DataType">
        <h3>{ props.name }</h3>
        <h3>{ props.types }</h3>
    </div>
}

export function Command(props) {
    return <div className="Command">
        <h3>{props.command}</h3>
        <div className="Arguments">
            { props.arguments.map(args =>
                <CommandArgument key={args.name} name={args.name} type={args.type} required={args.required} />) }
        </div>
        <DataType name="Input" types={ props.inputType } />
        <DataType name="Output" types={ props.outputType } />
        </div>;
}

export function Commands(commands, commandPreview, showCommand) {

    return <div className="Commands">
        { commands.map(raw => <button onClick={showCommand(raw)}>{raw.command}</button>) }
        { commandPreview === undefined ? '' : Command(commandPreview)}
    </div>;
}
